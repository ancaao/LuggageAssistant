package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.example.luggageassistant.viewmodel.MainViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView textView;
    private Button buttonLogout, buttonViewAccount, buttonAddLuggage;
    private boolean shouldResetTripConfiguration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(MainViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        textView = findViewById(R.id.user_details);
        buttonLogout = findViewById(R.id.btn_logout);
        buttonViewAccount = findViewById(R.id.btn_view_account);
        buttonAddLuggage = findViewById(R.id.btn_add_luggage);

        // Observăm statusul utilizatorului
        mainViewModel.getUserEmail().observe(this, email -> {
            if (email == null) {
                redirectToLogin();
            } else {
                textView.setText("Hello " + email);
            }
        });

        // Observăm statusul de logout
        mainViewModel.getLogoutStatus().observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                redirectToLogin();
            }
        });

        buttonLogout.setOnClickListener(view -> mainViewModel.logoutUser());

        buttonViewAccount.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        buttonAddLuggage.setOnClickListener(view -> {
            shouldResetTripConfiguration = true; // Marchez că trebuie să resetăm
            tripConfigurationViewModel.resetTripConfiguration();
            Intent intent = new Intent(MainActivity.this, StepOneActivity.class);
            startActivity(intent);
        });

        mainViewModel.checkIfUserIsLoggedIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldResetTripConfiguration) {
            tripConfigurationViewModel.resetTripConfiguration();
            shouldResetTripConfiguration = false;
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
