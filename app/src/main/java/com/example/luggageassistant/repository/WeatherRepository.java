package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.constants.Constants;
import com.example.luggageassistant.model.GeocodingResponse;
import com.example.luggageassistant.model.OneCallResponse;
import com.example.luggageassistant.model.WeatherForecastResponse;
import static com.example.luggageassistant.constants.Constants.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRepository {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String FORECAST_16_API_KEY = Constants.FORECAST_16_API_KEY;
    private static final String APPROXIMATE_API_KEY = Constants.APPROXIMATE_API_KEY;

    private final WeatherApi forecastApi;
    private final WeatherApi climateApi;
    private final WeatherApi geocodingApi;

    public WeatherRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit forecastRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        Retrofit climateRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/3.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        Retrofit geocodingRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        forecastApi = forecastRetrofit.create(WeatherApi.class);
        climateApi = climateRetrofit.create(WeatherApi.class);
        geocodingApi = geocodingRetrofit.create(WeatherApi.class);

    }

    public void get16DayForecast(String city, ForecastCallback callback) {
        forecastApi.getDailyForecast(city, 16, FORECAST_16_API_KEY, "metric")
                .enqueue(new Callback<WeatherForecastResponse>() {
                    @Override
                    public void onResponse(Call<WeatherForecastResponse> call, Response<WeatherForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Forecast error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherForecastResponse> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void getApproximateForecast(double lat, double lon, String date, LongTermForecastCallback callback) {
        climateApi.getLongTermForecast(lat, lon, date, APPROXIMATE_API_KEY, "metric")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String json = response.body().string();
                                Log.d("API_RAW_JSON", "Forecast JSON for " + date + ": " + json);
                                callback.onSuccess(json);
                            } catch (IOException e) {
                                callback.onError("Parse error");
                            }
                        } else {
                            callback.onError("Long forecast error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void getCoordinates(String city, String countryCode, CoordinatesCallback callback) {
        String query = city + "," + countryCode;
        geocodingApi.getCoordinatesFromCity(query, 1, APPROXIMATE_API_KEY)
                .enqueue(new Callback<List<GeocodingResponse>>() {
                    @Override
                    public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            GeocodingResponse geo = response.body().get(0);
                            callback.onSuccess(geo.lat, geo.lon);
                        } else {
                            callback.onError("Location not found: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public interface CoordinatesCallback {
        void onSuccess(double lat, double lon);
        void onError(String error);
    }

    public interface LongTermForecastCallback {
        void onSuccess(String jsonResponse);
        void onError(String error);
    }

    // Callback pentru forecast
    public interface ForecastCallback {
        void onSuccess(WeatherForecastResponse response);
        void onError(String error);
    }
}

