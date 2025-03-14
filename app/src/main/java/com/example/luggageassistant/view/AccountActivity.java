package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.User;
import com.example.luggageassistant.viewmodel.AccountViewModel;

public class AccountActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneTextView;
    private Button logoutButton;
    private AccountViewModel accountViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        accountViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(AccountViewModel.class);

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        logoutButton = findViewById(R.id.btn_logout);

        accountViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                nameTextView.setText(user.getFirstName() + " " + user.getLastName());
                emailTextView.setText(user.getEmail());
                phoneTextView.setText(user.getPhoneNo());
            } else {
                Toast.makeText(AccountActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });

        accountViewModel.getLogoutStatus().observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        logoutButton.setOnClickListener(view -> accountViewModel.logoutUser());

        // Încărcăm datele utilizatorului
        accountViewModel.loadUserData();
    }
}
