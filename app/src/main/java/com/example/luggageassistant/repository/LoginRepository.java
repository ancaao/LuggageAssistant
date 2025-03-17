package com.example.luggageassistant.repository;

import com.google.firebase.auth.FirebaseAuth;
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
                        callback.onLoginResult(false, "Authentication failed");
                    }
                });
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

}
