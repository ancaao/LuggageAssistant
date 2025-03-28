package com.example.luggageassistant.viewmodel;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.TripConfiguration;

public class TripConfigurationViewModel extends ViewModel {
    private TripConfiguration tripConfiguration;

    public TripConfigurationViewModel() {
        tripConfiguration = new TripConfiguration();
    }

    public void updateData(String key, Object value) {
    }

    public TripConfiguration getTripConfiguration() {
        return tripConfiguration;
    }

    public void saveTripConfiguration() {
    }
}