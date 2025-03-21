package com.example.luggageassistant.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginRepository {
    private final FirebaseAuth mAuth;

    public interface LoginCallback {
        void onLoginResult(boolean success, String errorMessage);
    }

    public LoginRepository() {
        mAuth = FirebaseAuth.getInstance();
    }
    public void login(String email, String password, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            callback.onLoginResult(true, null);
                        } else {
                            callback.onLoginResult(false, "User data not found");
                        }
                    } else {
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            callback.onLoginResult(false, "Email not found");
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            callback.onLoginResult(false, "Incorrect password");
                        } else {
                            callback.onLoginResult(false, "Authentication failed");
                        }
                    }
                });
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

}
