package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

public class MainRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public MainRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    public String getUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }
    public void getUserData(Consumer<User> onSuccess, Consumer<Exception> onFailure) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            onSuccess.accept(user);
                        } else {
                            onFailure.accept(new Exception("User document not found"));
                        }
                    })
                    .addOnFailureListener(onFailure::accept);
        } else {
            onFailure.accept(new Exception("User not logged in"));
        }
    }

    public void logout() {
        mAuth.signOut();
    }
}
