package com.example.luggageassistant.viewmodel;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.Luggage;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripConfigurationViewModel extends ViewModel {
    private TripConfiguration tripConfiguration;
    private TripConfigurationRepository tripConfigurationRepository;
    public MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private List<String> selectedPreferences = new ArrayList<>();

    public TripConfigurationViewModel() {
        tripConfigurationRepository = TripConfigurationRepository.getInstance();
        tripConfiguration = tripConfigurationRepository.getTripConfiguration();
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
        tripConfiguration.setCountry(country);
        tripConfiguration.setCity(city);
        tripConfiguration.setTripStartDate(startDateStr);
        tripConfiguration.setTripEndDate(endDateStr);
    }

    public void setTravelPurpose(List<String> travelPurpose) {
        tripConfiguration.setTravelPurpose(travelPurpose != null && !travelPurpose.isEmpty() ? travelPurpose.get(0) : null);
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

    public void saveTripConfiguration() {
        tripConfigurationRepository.saveTripConfiguration(tripConfiguration, new TripConfigurationRepository.OnDataSavedCallback() {
            @Override
            public void onSuccess() {
                messageLiveData.postValue("Data saved successfully!");
            }

            @Override
            public void onError(Exception e) {
                messageLiveData.postValue("Error saving data: " + e.getMessage());
            }
        });
    }

    public TripConfiguration getTripConfiguration() {
        return tripConfiguration;
    }
    public void setTripConfiguration(TripConfiguration newConfiguration) {
        this.tripConfiguration = newConfiguration;
    }

    public void setSelectedCountry(String country) {
        tripConfiguration.setCountry(country);
    }
}