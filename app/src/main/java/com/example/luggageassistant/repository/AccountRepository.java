package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AccountRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface UserCallback {
        void onComplete(User user);
    }

    public void getUserData(UserCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onComplete(null);
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setId(currentUser.getUid());
                        user.setFirstName(documentSnapshot.getString("firstName"));
                        user.setLastName(documentSnapshot.getString("lastName"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setPhoneNo(documentSnapshot.getString("phoneNo"));
                        callback.onComplete(user);
                    } else {
                        callback.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> callback.onComplete(null));
    }

    public void logout() {
        mAuth.signOut();
    }
}
