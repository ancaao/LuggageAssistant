package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private TextView textView;
    private Button buttonLogout, buttonViewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(MainViewModel.class);

        textView = findViewById(R.id.user_details);
        buttonLogout = findViewById(R.id.btn_logout);
        buttonViewAccount = findViewById(R.id.btn_view_account);

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

        mainViewModel.checkIfUserIsLoggedIn();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
