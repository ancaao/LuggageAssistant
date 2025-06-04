package com.example.luggageassistant.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.OneCallResponse;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.model.WeatherForecastResponse;
import com.example.luggageassistant.repository.WeatherRepository;
import com.example.luggageassistant.utils.WeatherCardHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherViewModel extends ViewModel {

    private final WeatherRepository weatherRepository = new WeatherRepository();
    private final MutableLiveData<List<WeatherForecastResponse.ForecastDay>> forecastLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> longTermForecastJson = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<String, Destination>> coordinatesResult = new MutableLiveData<>();

    public LiveData<List<WeatherForecastResponse.ForecastDay>> getForecastLiveData() {
        return forecastLiveData;
    }

    public LiveData<String> getLongTermForecastJson() {
        return longTermForecastJson;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Pair<String, Destination>> getCoordinatesResult() {
        return coordinatesResult;
    }

    // ✅ Apel pentru forecast pe 16 zile
    public void load16DayForecast(String city) {
        weatherRepository.get16DayForecast(city, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(WeatherForecastResponse response) {
                forecastLiveData.postValue(response.list);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

    // ✅ Apel pentru forecast estimativ (1.5 ani) pentru o dată anume
    public void loadApproximateForecast(double lat, double lon, String date) {
        weatherRepository.getApproximateForecast(lat, lon, date, new WeatherRepository.LongTermForecastCallback() {
            @Override
            public void onSuccess(String jsonResponse) {
                longTermForecastJson.postValue(date + "|" + jsonResponse);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

    public void loadCoordinates(String city, String countryCode, Destination destination) {
        weatherRepository.getCoordinates(city, countryCode, new WeatherRepository.CoordinatesCallback() {
            @Override
            public void onSuccess(double lat, double lon) {
                String coordStr = "Lat: " + lat + ", Lon: " + lon;
                coordinatesResult.postValue(new Pair<>(coordStr, destination));
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Geocoding error: " + error);
            }
        });
    }
}
