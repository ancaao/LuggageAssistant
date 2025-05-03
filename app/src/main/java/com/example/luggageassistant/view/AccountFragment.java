package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.viewmodel.AccountViewModel;

public class AccountFragment extends Fragment {

    private TextView nameTextView, emailTextView, phoneTextView;
    private Button logoutButton;
    private AccountViewModel accountViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        logoutButton = view.findViewById(R.id.btn_logout);

        accountViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(AccountViewModel.class);

        accountViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                nameTextView.setText(user.getFirstName() + " " + user.getLastName());
                emailTextView.setText(user.getEmail());
                phoneTextView.setText(user.getPhoneNo());
            } else {
                Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });

        accountViewModel.getLogoutStatus().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        logoutButton.setOnClickListener(v -> accountViewModel.logoutUser());

        accountViewModel.loadUserData();

        return view;
    }
}
