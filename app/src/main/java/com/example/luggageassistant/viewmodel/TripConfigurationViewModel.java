package com.example.luggageassistant.viewmodel;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.Luggage;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;

import java.util.ArrayList;
import java.util.List;

public class TripConfigurationViewModel extends ViewModel {
    private TripConfiguration tripConfiguration;
    private TripConfigurationRepository tripConfigurationRepository;
    public MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private List<String> selectedPreferences = new ArrayList<>();
    private final MutableLiveData<List<Destination>> destinations = new MutableLiveData<>(new ArrayList<>());

    private String userId;
    private String tripId;
    private final MutableLiveData<List<String>> cachedCities = new MutableLiveData<>();

    public TripConfigurationViewModel() {
        tripConfigurationRepository = TripConfigurationRepository.getInstance();
        tripConfiguration = tripConfigurationRepository.getTripConfiguration();
    }

    public void setCachedCities(List<String> cities) {
        cachedCities.setValue(cities);
    }

    public List<String> getCachedCities() {
        return cachedCities.getValue();
    }

    public void updateFormStepOne(String name, int age, String gender, List<String> preferences, List<TravelPartner> partners) {
        tripConfiguration.setName(name);
        tripConfiguration.setAge(age);
        tripConfiguration.setGender(gender);
        tripConfiguration.setSpecialPreferences(preferences);
        tripConfiguration.setPartner(partners);
        setSelectedPreferences(preferences);
    }

    public void updateFormStepTwo(List<Luggage> luggages) {
        List<Luggage> current = tripConfiguration.getLuggage();
        if (current == null) current = new ArrayList<>();
        current.addAll(luggages);
        tripConfiguration.setLuggage(current);
    }

    public void updateFormStepThree(String country, String city, String startDateStr, String endDateStr) {
        Destination destination = new Destination(country, city, startDateStr, endDateStr);
        List<Destination> current = destinations.getValue();
        if (current == null) current = new ArrayList<>();

        if (current.isEmpty()) {
            current.add(destination); // prima destinație
        } else {
            current.set(0, destination); // înlocuiește prima
        }

        destinations.setValue(current);
        tripConfiguration.setDestinations(current); // salvează și în config
    }

    public void setSelectedCountry(String country) {
        List<Destination> current = destinations.getValue();
        if (current == null || current.isEmpty()) {
            Destination newDestination = new Destination();
            newDestination.setCountry(country);
            current = new ArrayList<>();
            current.add(newDestination);
        } else {
            current.get(0).setCountry(country);
        }
        destinations.setValue(current);
        tripConfiguration.setDestinations(current);
    }


    public LiveData<List<Destination>> getDestinations() {
        return destinations;
    }

    public void addDestination(Destination destination) {
        List<Destination> current = new ArrayList<>(destinations.getValue());
        current.add(destination);
        destinations.setValue(current);
    }

    public void removeDestination(int index) {
        List<Destination> current = new ArrayList<>(destinations.getValue());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            destinations.setValue(current);
        }
    }

    public void resetTripConfiguration() {
        tripConfigurationRepository.resetTripConfiguration();
        this.tripConfiguration = tripConfigurationRepository.getTripConfiguration();
    }

    public void setTravelPurpose(List<String> travelPurpose) {
        tripConfiguration.setTravelPurpose(travelPurpose);
    }

    public void setPlannedActivities(List<String> activities) {
        tripConfiguration.setPlannedActivities(activities);
    }

    public void setSpecialEvents(List<String> events) {
        tripConfiguration.setSpecialEvents(events);
    }

    public List<String> getSelectedPreferences() {
        return selectedPreferences;
    }

    private void setSelectedPreferences(List<String> preferences) {
        this.selectedPreferences.clear();
        this.selectedPreferences.addAll(preferences);
    }

    public TripConfiguration getTripConfiguration() {
        tripConfiguration.setDestinations(destinations.getValue());
        return tripConfiguration;
    }

    public void setTripConfiguration(TripConfiguration newConfiguration) {
        this.tripConfiguration = newConfiguration;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}