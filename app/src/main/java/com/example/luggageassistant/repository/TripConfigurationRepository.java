package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.TripConfiguration;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripConfigurationRepository {
    private FirebaseFirestore db;

    public TripConfigurationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveTripConfiguration(TripConfiguration tripConfiguration, OnDataSavedCallback callback) {
        db.collection("tripConfigurations").add(tripConfiguration.toMap())
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e));
    }

    public interface OnDataSavedCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
