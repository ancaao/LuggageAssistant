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
        List<Destination> current = tripConfiguration.getDestinations();
        if (current == null) current = new ArrayList<>();

        if (current.isEmpty()) {
            current.add(destination);
        } else {
            current.set(0, destination);
        }

        tripConfiguration.setDestinations(current);
    }
    public void setSelectedCountry(String country) {
        List<Destination> current = tripConfiguration.getDestinations();
        if (current == null || current.isEmpty()) {
            Destination newDestination = new Destination();
            newDestination.setCountry(country);
            current = new ArrayList<>();
            current.add(newDestination);
        } else {
            current.get(0).setCountry(country);
        }
        tripConfiguration.setDestinations(current);
    }
    public void addDestination(Destination destination) {
        List<Destination> current = tripConfiguration.getDestinations();
        if (current == null) current = new ArrayList<>();
        current.add(destination);
        tripConfiguration.setDestinations(current);
    }
    public void clearDestinations() {
        tripConfiguration.setDestinations(new ArrayList<>());
    }
    public void removeDestination(int index) {
        List<Destination> current = tripConfiguration.getDestinations();
        if (current != null && index >= 0 && index < current.size()) {
            current.remove(index);
            tripConfiguration.setDestinations(current);
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