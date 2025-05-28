package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.OneCallResponse;
import com.example.luggageassistant.model.WeatherForecastResponse;
import com.example.luggageassistant.repository.WeatherRepository;

import java.util.List;

public class WeatherViewModel extends ViewModel {

    private final WeatherRepository weatherRepository = new WeatherRepository();
    private final MutableLiveData<List<WeatherForecastResponse.ForecastDay>> forecastLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> longTermForecastJson = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<OneCallResponse.DailyForecast>> forecast8LiveData = new MutableLiveData<>();
    private final MutableLiveData<String> coordinatesResult = new MutableLiveData<>();

    public LiveData<List<WeatherForecastResponse.ForecastDay>> getForecastLiveData() {
        return forecastLiveData;
    }

    public LiveData<String> getLongTermForecastJson() {
        return longTermForecastJson;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<List<OneCallResponse.DailyForecast>> getForecast8LiveData() {
        return forecast8LiveData;
    }

    public LiveData<String> getCoordinatesResult() {
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
                longTermForecastJson.postValue(jsonResponse);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

    public void loadCoordinates(String city, String countryCode) {
        weatherRepository.getCoordinates(city, countryCode, new WeatherRepository.CoordinatesCallback() {
            @Override
            public void onSuccess(double lat, double lon) {
                coordinatesResult.postValue("Lat: " + lat + ", Lon: " + lon);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Geocoding error: " + error);
            }
        });
    }
}
