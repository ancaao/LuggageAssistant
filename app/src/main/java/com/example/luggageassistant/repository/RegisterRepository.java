package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public RegisterRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface RegistrationCallback {
        void onComplete(boolean success);
    }

    public void registerUser(String email, String password, String firstName, String lastName, String phone, RegistrationCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            User newUser = new User();
                            newUser.setId(user.getUid());
                            newUser.setFirstName(firstName);
                            newUser.setLastName(lastName);
                            newUser.setEmail(email);
                            newUser.setPhoneNo(phone);

                            db.collection("users").document(user.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> callback.onComplete(true))
                                    .addOnFailureListener(e -> callback.onComplete(false));
                        } else {
                            callback.onComplete(false);
                        }
                    } else {
                        callback.onComplete(false);
                    }
                });
    }

    public void checkIfEmailExists(String email, EmailCheckCallback callback) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean exists = !task.getResult().isEmpty();
                        callback.onCheckComplete(exists);
                    } else {
                        callback.onCheckComplete(false);
                    }
                });
    }
}
