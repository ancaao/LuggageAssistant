package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.google.android.material.textfield.TextInputEditText;
import com.example.luggageassistant.viewmodel.RegisterViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextPhone, editTextEmail, editTextPassword;
    private TextInputLayout firstNameLayout, lastNameLayout, phoneLayout ,emailLayout, passwordLayout;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private RegisterViewModel registerViewModel;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        registerViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RegisterViewModel.class);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextPhone = findViewById(R.id.phoneNo);

        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        phoneLayout = findViewById(R.id.phoneNoLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.loginNow);

        editTextEmail.addTextChangedListener(new InputValidator(emailLayout, editTextEmail));
        editTextPassword.addTextChangedListener(new InputValidator(passwordLayout, editTextPassword));

        setFocusChangeListener(editTextEmail, emailLayout, (input, layout) -> resetError(emailLayout, input));
        setFocusChangeListener(editTextPassword, passwordLayout, (input, layout) -> resetError(passwordLayout, input));

        textViewLogin.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        buttonRegister.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phone)) {
                Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email, emailLayout) || !isValidPassword(password, passwordLayout)) {
                return;
            }

            registerViewModel.registerUser(email, password, firstName, lastName, phone);
        });

        registerViewModel.getIsLoading().observe(this, isLoading -> progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        registerViewModel.getRegistrationStatus().observe(this, success -> {
            if (success) {
                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class InputValidator implements TextWatcher {
        private final TextInputLayout layout;
        private final TextInputEditText editText;

        public InputValidator(TextInputLayout layout, TextInputEditText editText) {
            this.layout = layout;
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateInput();
        }

        @Override
        public void afterTextChanged(Editable s) {}

        private void validateInput() {
            String input = editText.getText().toString().trim();
            boolean isValid = false;

            if (editText.getId() == R.id.email) {
                isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
                layout.setError(isValid ? null : "Invalid email address!");
            }
            else if (editText.getId() == R.id.password) {
                String passwordPattern = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}";
                isValid = input.matches(passwordPattern);
                layout.setError(isValid ? null : "Password must contain: 8 characters, uppercase, lowercase, number, special character.");
            }

            int color = isValid ? ContextCompat.getColor(RegisterActivity.this, R.color. green)
                    : ContextCompat.getColor(RegisterActivity.this, R.color.red);
            layout.setBoxStrokeColor(color);
        }
    }

    // Resetarea erorilor când utilizatorul părăsește câmpul
    private void resetError(TextInputLayout layout, String input) {
        if (!TextUtils.isEmpty(input)) {
            boolean isValid = (layout == emailLayout) ? isValidEmail(input, layout) : isValidPassword(input, layout);
            if (isValid) {
                layout.setBoxStrokeColor(ContextCompat.getColor(RegisterActivity.this, R.color.green));
            }
        } else {
            layout.setBoxStrokeColor(ContextCompat.getColor(RegisterActivity.this, R.color.grey)); // Reset la culoarea normală
        }
    }
    private void setFocusChangeListener(TextInputEditText editText, TextInputLayout layout, ValidationFunction validationFunction) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validationFunction.validate(editText.getText().toString().trim(), layout);
            }
        });
    }
    interface ValidationFunction {
        void validate(String input, TextInputLayout layout);
    }

    // Email validation (returns boolean)
    private boolean isValidEmail(String email, TextInputLayout emailLayout) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email address!");
            emailLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.red));
            return false;
        } else {
            emailLayout.setError(null);
            emailLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.green));
            return true;
        }
    }

    // Password validation (returns boolean)
    private boolean isValidPassword(String password, TextInputLayout passwordLayout) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        if (!password.matches(passwordPattern)) {
            passwordLayout.setError("Password must contain:\n- 8 characters \n- an uppercase letter \n- a lowercase letter \n- a number \n- a special character");
            passwordLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.red));
            return false;
        } else {
            passwordLayout.setError(null);
            passwordLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.green));
            return true;
        }
    }
}
