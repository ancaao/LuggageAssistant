package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.TripConfiguration;

import java.util.List;

public interface OnTripConfigurationsLoadedListener {
    void onTripsLoaded(List<TripConfiguration> trips);
    void onError(Exception e);
}

