package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.TripConfiguration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TripConfigurationRepository {
    private final FirebaseFirestore db;
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

    public void saveTripConfiguration(String userId, String tripId, TripConfiguration tripConfiguration, OnDataSavedCallback callback) {
        try {
            Map<String, Object> map = tripConfiguration.toMap();
            Log.d("DESTINATIONS_DEBUG", "Saving trip with destinations: " + (tripConfiguration.getDestinations() != null ? tripConfiguration.getDestinations().size() : "null"));
            db.collection("users")
                    .document(userId)
                    .collection("trips")
                    .document(tripId)
                    .set(map)
                    .addOnSuccessListener(unused -> {
                        Log.d("DESTINATIONS_DEBUG", "Trip saved directly under trips/" + tripId);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DESTINATIONS_DEBUG", "Failed to save trip", e); // ⬅️ debug
                        callback.onError(e);
                    });
        } catch (Exception e) {
            Log.e("DESTINATIONS_DEBUG", "Exception in saveTripConfiguration", e); // ⬅️ debug
            callback.onError(e);
        }
    }
    public void getAllTripConfigurations(String userId, OnTripConfigurationsLoadedListener listener) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<TripConfiguration> tripList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        TripConfiguration config = doc.toObject(TripConfiguration.class);
                        config.setTripId(doc.getId());
                        tripList.add(config);
                    }
                    listener.onTripsLoaded(tripList);
                })
                .addOnFailureListener(listener::onError);
    }

    public void deleteTripConfiguration(String userId, String tripId, Runnable onSuccess) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d("TRIP_DELETE", "Trip " + tripId + " deleted.");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("TRIP_DELETE", "Error deleting trip", e));
    }

    public void updateTripPinned(String userId, String tripId, boolean pinned, Runnable onSuccess) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .update("pinned", pinned)
                .addOnSuccessListener(unused -> {
                    Log.d("PIN_TRIP", "Trip " + tripId + " updated with pinned=" + pinned);
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("PIN_TRIP", "Failed to update pinned state", e));
    }

    public void resetTripConfiguration() {
        this.tripConfiguration = new TripConfiguration();
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

//    public interface OnTripConfigurationsLoadedListener {
//        void onTripsLoaded(List<TripConfiguration> trips);
//        void onError(Exception e);
//    }
}
