package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.example.luggageassistant.viewmodel.MainViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView textView;
    private Button buttonLogout, buttonViewAccount, buttonAddLuggage;

    private boolean shouldResetTripConfiguration = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mainViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(MainViewModel.class);

        tripConfigurationViewModel = new ViewModelProvider(requireActivity()).get(TripConfigurationViewModel.class);

        textView = view.findViewById(R.id.user_details);
        buttonLogout = view.findViewById(R.id.btn_logout);
        buttonViewAccount = view.findViewById(R.id.btn_view_account);
        buttonAddLuggage = view.findViewById(R.id.btn_add_luggage);

        mainViewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            if (email == null) {
                redirectToLogin();
            } else {
                textView.setText("Hello " + email);
            }
        });

        mainViewModel.getLogoutStatus().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                redirectToLogin();
            }
        });

        buttonLogout.setOnClickListener(v -> mainViewModel.logoutUser());

        buttonViewAccount.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_account);
        });

        buttonAddLuggage.setOnClickListener(v -> {
            shouldResetTripConfiguration = true;
            tripConfigurationViewModel.resetTripConfiguration();
            Intent intent = new Intent(requireActivity(), StepOneActivity.class);
            startActivity(intent);
        });

        mainViewModel.checkIfUserIsLoggedIn();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldResetTripConfiguration) {
            Log.d("HomeFragment", "Resetting Trip Configuration");
            tripConfigurationViewModel.resetTripConfiguration();
            shouldResetTripConfiguration = false;
        }
    }


    private void redirectToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
