package com.example.luggageassistant.viewmodel;

import android.annotation.SuppressLint;
import android.net.ParseException;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.OnTripConfigurationsLoadedListener;
import com.example.luggageassistant.repository.TripConfigurationRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarViewModel extends ViewModel {

    private final MutableLiveData<List<TripConfiguration>> tripsLiveData = new MutableLiveData<>();
    private final Map<LocalDate, List<TripConfiguration>> vacationDaysMap = new HashMap<>();
    @SuppressLint("NewApi")
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LiveData<List<TripConfiguration>> getTripsLiveData() {
        return tripsLiveData;
    }

    public void loadTrips(String userId) {
        TripConfigurationRepository.getInstance().getAllTripConfigurations(userId, new OnTripConfigurationsLoadedListener() {

            @Override
            public void onTripsLoaded(List<TripConfiguration> trips) {
                tripsLiveData.setValue(trips);
                buildVacationMap(trips);
            }

            @Override
            public void onError(Exception e) {
                // poți trimite un event separat pentru eroare
            }
        });
    }

    @SuppressLint("NewApi")
    private void buildVacationMap(List<TripConfiguration> trips) {
        vacationDaysMap.clear();
        for (TripConfiguration trip : trips) {
            if (trip.getDestinations() == null) continue;

            for (Destination dest : trip.getDestinations()) {
                try {
                    LocalDate start = LocalDate.parse(dest.getTripStartDate(), formatter);
                    LocalDate end = LocalDate.parse(dest.getTripEndDate(), formatter);
                    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                        vacationDaysMap.computeIfAbsent(date, d -> new ArrayList<>()).add(trip);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public List<TripConfiguration> getTripsForDate(LocalDate date) {
        return vacationDaysMap.getOrDefault(date, new ArrayList<>());
    }

    @SuppressLint("NewApi")
    public List<Pair<TripConfiguration, Destination>> getDestinationsForDate(LocalDate clickedDate) {
        List<Pair<TripConfiguration, Destination>> results = new ArrayList<>();
        String clickedDateStr = clickedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        for (TripConfiguration trip : tripsLiveData.getValue()) {
            for (Destination destination : trip.getDestinations()) {
                if (isDateInRange(clickedDateStr, destination.getTripStartDate(), destination.getTripEndDate())) {
                    results.add(new Pair<>(trip, destination));
                }
            }
        }

        return results;
    }

    private boolean isDateInRange(String targetDateStr, String startDateStr, String endDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date targetDate = sdf.parse(targetDateStr);
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
            return !targetDate.before(startDate) && !targetDate.after(endDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<LocalDate, List<TripConfiguration>> getVacationDaysMap() {
        return vacationDaysMap;
    }

}

