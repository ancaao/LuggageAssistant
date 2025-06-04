package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.GeocodingResponse;
import com.example.luggageassistant.model.OneCallResponse;
import com.example.luggageassistant.model.WeatherForecastResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("forecast/daily")
    Call<WeatherForecastResponse> getDailyForecast(
            @Query("q") String location,
            @Query("cnt") int days,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("onecall/day_summary")
    Call<ResponseBody> getLongTermForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("date") String date,     // Format: yyyy-MM-dd
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    @GET("geo/1.0/direct")
    Call<List<GeocodingResponse>> getCoordinatesFromCity(
            @Query("q") String location,          // ex: "Paris,FR"
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}

