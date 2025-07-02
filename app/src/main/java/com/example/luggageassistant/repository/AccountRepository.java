package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.luggageassistant.view.AccountFragment;

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

    public void deleteAccount(String password, AccountFragment.Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onComplete(false);
            return;
        }

        String email = currentUser.getEmail();
        if (email == null || password == null) {
            callback.onComplete(false);
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(authResult -> {
                    db.collection("users").document(currentUser.getUid())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                currentUser.delete()
                                        .addOnSuccessListener(unused1 -> callback.onComplete(true))
                                        .addOnFailureListener(e -> callback.onComplete(false));
                            })
                            .addOnFailureListener(e -> callback.onComplete(false));
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteAccount", "Reauth failed: " + e.getMessage());
                    callback.onComplete(false);
                });
    }

    public void sendPasswordResetEmail(String email, AccountFragment.Callback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void updateUserField(String field, String value) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .update(field, value);
        }
    }
    public void logout() {
        mAuth.signOut();
    }
}
