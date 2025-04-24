package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.TripConfiguration;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripConfigurationRepository {
    private FirebaseFirestore db;
    private static TripConfigurationRepository instance;
    private TripConfiguration tripConfiguration;

    private TripConfigurationRepository() {
        db = FirebaseFirestore.getInstance();
        tripConfiguration = new TripConfiguration();
    }

    public static synchronized TripConfigurationRepository getInstance() {
        if (instance == null) {
            instance = new TripConfigurationRepository();
        }
        return instance;
    }
    public void saveTripConfiguration(TripConfiguration tripConfiguration, OnDataSavedCallback callback) {
        db.collection("tripConfigurations").add(tripConfiguration.toMap())
                .addOnSuccessListener(documentReference -> {
                    Log.d("FIREBASE", "Trip saved with ID: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error saving trip", e);
                    callback.onError(e);
                });
    }

    public TripConfiguration getTripConfiguration() {
        return tripConfiguration;
    }

    public void setTripConfiguration(TripConfiguration tripConfiguration) {
        this.tripConfiguration = tripConfiguration;
    }
    public interface OnDataSavedCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
