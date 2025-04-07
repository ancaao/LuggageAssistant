package com.example.luggageassistant.viewmodel;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
}