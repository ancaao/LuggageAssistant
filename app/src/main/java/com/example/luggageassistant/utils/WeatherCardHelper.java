package com.example.luggageassistant.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;

import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.model.WeatherApproximateForecast;
import com.example.luggageassistant.model.WeatherForecastResponse;
import com.example.luggageassistant.viewmodel.WeatherViewModel;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class WeatherCardHelper {
    private static final float[] globalMaxTemps = {Float.MAX_VALUE, Float.MIN_VALUE};
    private static final List<String> globalCityLabels = new ArrayList<>();
    public static void processAndDisplayAggregatedWeather(
            CardView weatherCard,
            TextView weatherLocation,
            TextView weatherTemperature,
            TripConfiguration trip,
            WeatherViewModel viewModel,
            LifecycleOwner lifecycleOwner) {

        List<Destination> destinations = trip.getDestinations();
        if (destinations == null || destinations.isEmpty()) {
            weatherCard.setVisibility(View.GONE);
            return;
        }

        Date today = new Date();
        long todayMillis = today.getTime();

        for (Destination dest : destinations) {
            String city = dest.getCity();
            String country = dest.getCountry();

            if (!globalCityLabels.contains(city)) {
                globalCityLabels.add(city);
            }
            List<String> dateRange = generateDateRange(dest.getTripStartDate(), dest.getTripEndDate());

            boolean needsShortTerm = false;
            boolean needsLongTerm = false;

            for (String dateStr : dateRange) {
                try {
                    Date day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                    long diffDays = (day.getTime() - todayMillis) / (1000 * 60 * 60 * 24);

                    if (diffDays < 0 || diffDays > 548) continue;

                    if (diffDays <= 14) {
                        needsShortTerm = true;
                    } else {
                        needsLongTerm = true;
                    }

                } catch (ParseException e) {
                    Log.e("FORECAST_DEBUG", "Bad date", e);
                }
            }

            // Apelăm o singură dată pe oraș
            if (needsShortTerm) {
                Log.d("FORECAST_DEBUG", "Loading 16-day forecast for " + city);
                viewModel.load16DayForecast(city);
            }
            if (needsLongTerm) {
                Log.d("FORECAST_DEBUG", "Loading coordinates for long-term forecast for " + city);
                viewModel.loadCoordinates(city, country, dest); // trimitem Destination
            }
        }
    }
    private static void showAggregatedCard(CardView card, TextView title, TextView temp,
                                           List<String> cities, float minTemp, float maxTemp) {
        card.setVisibility(View.VISIBLE);
        title.setText("Your next trip to " + TextUtils.join(", ", cities));
        temp.setText(Math.round(minTemp) + "°C - " + Math.round(maxTemp) + "°C");
    }

    public static void displayCachedForecast(CardView card, TextView title, TextView temp,
                                             List<String> cities, float minTemp, float maxTemp) {
        showAggregatedCard(card, title, temp, cities, minTemp, maxTemp);
    }


    public static List<String> generateDateRange(String start, String end) {
        List<String> result = new ArrayList<>();
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(start));
            Date endDate = format.parse(end);

            while (!calendar.getTime().after(endDate)) {
                result.add(format.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void handleCoordinatesResult(String coordStr,
                                               Destination destination,
                                               WeatherViewModel viewModel) {
        if (destination == null) return;

        Date today = new Date();
        long todayMillis = today.getTime();

        try {
            String[] parts = coordStr.replace("Lat: ", "").replace("Lon: ", "").split(",");
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());

            List<String> dateRange = generateDateRange(destination.getTripStartDate(), destination.getTripEndDate());

            for (String dateStr : dateRange) {
                Date day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                long diffDays = (day.getTime() - todayMillis) / (1000 * 60 * 60 * 24);

                if (diffDays <= 14 || diffDays > 500) continue;

                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day);
                Log.d("LONG_FORECAST_DEBUG", "Requesting long-term forecast for " + destination.getCity() + " on " + formattedDate);
                viewModel.loadApproximateForecast(lat, lon, formattedDate);
            }

        } catch (Exception e) {
            Log.e("COORD_PARSE", "Coord parse fail", e);
        }
    }


    public static void processForecastList(List<WeatherForecastResponse.ForecastDay> forecastList,
                                           TripConfiguration trip,
                                           CardView weatherCard,
                                           TextView weatherLocation,
                                           TextView weatherTemperature) {
        if (forecastList == null || forecastList.isEmpty()) return;

        List<Destination> destinations = trip.getDestinations();
        if (destinations == null || destinations.isEmpty()) return;

        Date today = new Date();
        long todayMillis = today.getTime();

        for (Destination dest : destinations) {
            String city = dest.getCity();
            if (!globalCityLabels.contains(dest.getCity())) {
                globalCityLabels.add(dest.getCity());
            }


            List<String> dateRange = generateDateRange(dest.getTripStartDate(), dest.getTripEndDate());

            for (String dateStr : dateRange) {
                try {
                    Date day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                    long diffDays = (day.getTime() - todayMillis) / (1000 * 60 * 60 * 24);

                    if (diffDays < 0 || diffDays > 14 || forecastList.size() <= diffDays) continue;

                    WeatherForecastResponse.ForecastDay forecastDay = forecastList.get((int) diffDays);

                    // ✅ Log pentru verificare
                    Log.d("FORECAST_DEBUG", "City: " + city + ", Date: " + dateStr +
                            ", Min: " + forecastDay.temp.min + "°C, Max: " + forecastDay.temp.max + "°C");

//                    updateMaxOnly(globalMaxTemps, forecastDay.temp.max);
                    updateGlobalMinAndMax(forecastDay.temp.min, forecastDay.temp.max);
                } catch (ParseException e) {
                    Log.e("PARSE_ERR", "Bad date", e);
                }
            }
        }
    }

    public static void processLongTermJson(String json,
                                           TripConfiguration trip,
                                           CardView weatherCard,
                                           TextView weatherLocation,
                                           TextView weatherTemperature,
                                           String date) {

        List<Destination> destinations = trip.getDestinations();
        if (destinations == null || destinations.isEmpty()) return;


        try {
            JSONObject obj = new JSONObject(json);
            JSONObject temp = obj.getJSONObject("temperature");
            float min = (float) temp.getDouble("min");
            float max = (float) temp.getDouble("max");

            for (Destination dest : destinations) {
                List<String> dateRange = generateDateRange(dest.getTripStartDate(), dest.getTripEndDate());
                if (dateRange.contains(convertDateFormat(date))) {
                    if (!globalCityLabels.contains(dest.getCity())) {
                        globalCityLabels.add(dest.getCity());
                    }

                    Log.d("LONG_FORECAST_DEBUG", "City: " + dest.getCity() + ", Date: " + date +
                            ", Min: " + min + "°C, Max: " + max + "°C");
                }
            }

//            updateMaxOnly(globalMaxTemps, max);
            updateGlobalMinAndMax(min, max);
        } catch (JSONException e) {
            Log.e("PARSE_ERR", "Failed JSON", e);
        }
    }

    private static String convertDateFormat(String input) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(input);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return "";
        }
    }
    public static void finalizeAndDisplayWeatherCard(CardView weatherCard,
                                                     TextView weatherLocation,
                                                     TextView weatherTemperature,
                                                     String tripId) {
        if (globalMaxTemps[0] == Float.MAX_VALUE || globalMaxTemps[1] == Float.MIN_VALUE) {
            weatherCard.setVisibility(View.GONE);
            return;
        }

        showAggregatedCard(weatherCard, weatherLocation, weatherTemperature,
                globalCityLabels, globalMaxTemps[0], globalMaxTemps[1]);

        // ✅ SALVEAZĂ ÎN CACHE
        Context context = weatherCard.getContext();
        WeatherCacheHelper.saveAggregatedForecast(
                context,
                globalMaxTemps[0],
                globalMaxTemps[1],
                new ArrayList<>(globalCityLabels),
                tripId
        );

        // Resetăm valorile pentru următorul apel
        globalMaxTemps[0] = Float.MAX_VALUE;
        globalMaxTemps[1] = Float.MIN_VALUE;
        globalCityLabels.clear();
    }

    public static void resetGlobalWeatherState() {
        globalMaxTemps[0] = Float.MAX_VALUE;
        globalMaxTemps[1] = Float.MIN_VALUE;
        globalCityLabels.clear();
    }

    private static void updateGlobalMinAndMax(float min, float max) {
        globalMaxTemps[0] = Math.min(globalMaxTemps[0], min); // actualizează minimul
        globalMaxTemps[1] = Math.max(globalMaxTemps[1], max); // actualizează maximul
    }
}