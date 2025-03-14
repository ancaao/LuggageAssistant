package com.example.luggageassistant.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainRepository {

    private final FirebaseAuth mAuth;

    public MainRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    public String getUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    public void logout() {
        mAuth.signOut();
    }
}
