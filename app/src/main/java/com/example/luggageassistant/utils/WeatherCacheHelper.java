package com.example.luggageassistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherCacheHelper {

    private static final String PREF_NAME = "weather_aggregated_forecast";
    private static final String KEY_MIN_TEMP = "min_temp";
    private static final String KEY_MAX_TEMP = "max_temp";
    private static final String KEY_DATE = "forecast_date";
    private static final String KEY_CITIES = "forecast_cities";
    private static final String KEY_TRIP_ID = "trip_id";

    public static void saveAggregatedForecast(Context context, float minTemp, float maxTemp, List<String> cities, String tripId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_MIN_TEMP, minTemp)
                .putFloat(KEY_MAX_TEMP, maxTemp)
                .putString(KEY_DATE, getTodayDate())
                .putString(KEY_CITIES, TextUtils.join(",", cities))
                .putString(KEY_TRIP_ID, tripId)
                .apply();
    }

    public static boolean isCachedForToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_DATE, null);
        return getTodayDate().equals(savedDate);
    }

    public static float getMinTemp(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getFloat(KEY_MIN_TEMP, 0f);
    }

    public static float getMaxTemp(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getFloat(KEY_MAX_TEMP, 0f);
    }

    public static List<String> getCities(Context context) {
        String saved = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_CITIES, "");
        return Arrays.asList(saved.split(","));
    }

    private static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static boolean isValidForTodayTrip(Context context, String currentTripId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_DATE, null);
        String savedTripId = prefs.getString(KEY_TRIP_ID, null);
        return getTodayDate().equals(savedDate) && currentTripId.equals(savedTripId);
    }

    public static void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

