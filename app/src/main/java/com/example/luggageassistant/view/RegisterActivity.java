package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.luggageassistant.utils.InputValidator;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextPhone, editTextEmail, editTextPassword, editTextConfirmPassword;
    private TextInputLayout firstNameLayout, lastNameLayout, phoneLayout ,emailLayout, passwordLayout, confirmPasswordLayout;
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
        editTextConfirmPassword = findViewById(R.id.confirmPassword);

        firstNameLayout = findViewById(R.id.firstNameLayout);
        lastNameLayout = findViewById(R.id.lastNameLayout);
        phoneLayout = findViewById(R.id.phoneNoLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.loginNow);

        editTextEmail.addTextChangedListener(new InputValidator(this, emailLayout, editTextEmail));
        editTextPassword.addTextChangedListener(new InputValidator(this, passwordLayout, editTextPassword));
        editTextPhone.addTextChangedListener(new InputValidator(this, phoneLayout, editTextPhone));
//        editTextConfirmPassword.addTextChangedListener(new InputValidator(this, confirmPasswordLayout, editTextConfirmPassword));

        setFocusChangeListener(editTextEmail, emailLayout);
        setFocusChangeListener(editTextPassword, passwordLayout);
        setFocusChangeListener(editTextPhone, phoneLayout);
//        setFocusChangeListener(editTextConfirmPassword, confirmPasswordLayout);

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
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phone)) {
                Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isEmailValid = new InputValidator(this, emailLayout, editTextEmail).validateField();
            boolean isPasswordValid = new InputValidator(this, passwordLayout, editTextPassword).validateField();
            boolean isPhoneValid = new InputValidator(this, phoneLayout, editTextPhone).validateField();

            if (!isEmailValid || !isPasswordValid || !isPhoneValid) {
                return;
            }

            if (!doPasswordsMatch(password, confirmPassword)) {
                return;
            }

            progressBar.setVisibility(View.VISIBLE); // Afișează loading

            registerViewModel.checkIfEmailExists(email, exists -> {
                if (exists) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        emailLayout.setError("This email is already in use!");
                        emailLayout.setBoxStrokeColor(ContextCompat.getColor(RegisterActivity.this, R.color.error));
                    });
                } else {
                    runOnUiThread(() -> {
                        emailLayout.setError(null);
                        emailLayout.setBoxStrokeColor(ContextCompat.getColor(RegisterActivity.this, R.color.success));
                    });

                    // Înregistrăm utilizatorul doar dacă email-ul NU există
                    registerViewModel.registerUser(email, password, firstName, lastName, phone, success -> {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (success) {
                                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
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

    // Resetarea erorilor când utilizatorul părăsește câmpul
    private void resetError(TextInputLayout layout, TextInputEditText editText) {
        if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
            new InputValidator(this, layout, editText).validateField();
        } else {
            layout.setError(null);
            layout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary)); // Reset la culoarea normală
        }
    }

    private void setFocusChangeListener(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                resetError(layout, editText);
            }
        });
    }

    private boolean doPasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match!");
            confirmPasswordLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.error));
            return false;
        } else {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.success));
            return true;
        }
    }


}
