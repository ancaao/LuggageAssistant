package com.example.luggageassistant.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.model.WeatherForecastResponse;
import com.example.luggageassistant.model.OneCallResponse;
import com.example.luggageassistant.repository.OnTripConfigurationsLoadedListener;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.example.luggageassistant.view.adapter.HomeCombinedAdapter;
import com.example.luggageassistant.view.adapter.HomeTripCardAdapter;
import com.example.luggageassistant.viewmodel.MainViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.example.luggageassistant.viewmodel.WeatherViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView textView;
    private boolean shouldResetTripConfiguration = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mainViewModel = new ViewModelProvider(requireActivity(), new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(MainViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(requireActivity()).get(TripConfigurationViewModel.class);

        textView = view.findViewById(R.id.user_details);

        mainViewModel.loadUserData();

        mainViewModel.getUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                textView.setText("Hello, " + user.getFirstName() + "!");
            }
        });

        mainViewModel.checkIfUserIsLoggedIn();

//        WeatherViewModel viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // ✅ Testare 1: Prognoză pe 16 zile în București
//        viewModel.load16DayForecast("Bucharest");

        // ✅ Testare 2: Prognoză aproximativă pentru 1 noiembrie în București
        // București = lat: 44.4268, lon: 26.1025
//        viewModel.loadApproximateForecast(44.4268, 26.1025, "2026-10-26");

        // ✅ Observă rezultatele:
//        viewModel.getForecastLiveData().observe(getViewLifecycleOwner(), forecastList -> {
//            for (WeatherForecastResponse.ForecastDay day : forecastList) {
//                Log.d("FORECAST_16_DAYS", "Max: " + day.temp.max + ", Condiție: " + day.weather.get(0).description);
//            }
//        });

//        viewModel.getLongTermForecastJson().observe(getViewLifecycleOwner(), json -> {
//            Log.d("APPROX_FORECAST", "JSON: " + json);
//        });
//
//        viewModel.loadCoordinates("Bucharest", "RO");
//
//        viewModel.getCoordinatesResult().observe(getViewLifecycleOwner(), coords -> {
//            Log.d("GEO_RESULT", "Coordonate: " + coords);
//        });
//
//
//        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
//            Log.e("METEO_ERROR", "Eroare: " + error);
//        });

//        Button importCitiesButton = view.findViewById(R.id.importCitiesButton);
//        importCitiesButton.setOnClickListener(v -> importCitiesToFirestore());

        RecyclerView recyclerView = view.findViewById(R.id.home_combined_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TripConfigurationRepository.getInstance().getAllTripConfigurations(userId, new OnTripConfigurationsLoadedListener() {
            @Override
            public void onTripsLoaded(List<TripConfiguration> trips) {
                List<HomeCombinedAdapter.TripSection> sections = new ArrayList<>();

                List<TripConfiguration> pinned = new ArrayList<>();
                List<TripConfiguration> upcoming = new ArrayList<>();
                List<TripConfiguration> past = new ArrayList<>();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date today = new Date();

                for (TripConfiguration trip : trips) {
                    Date startDate = getFirstTripStartDate(trip);
                    if (startDate == null) continue;

                    if (trip.isPinned()) {
                        pinned.add(trip);
                    } else if (!startDate.before(today)) {
                        upcoming.add(trip);
                    } else {
                        past.add(trip);
                    }
                }

                Comparator<TripConfiguration> dateComparator = Comparator.comparing(
                        trip -> {
                            List<Destination> destinations = trip.getDestinations();
                            if (destinations != null && !destinations.isEmpty()) {
                                String dateStr = destinations.get(0).getTripStartDate();
                                if (dateStr != null && !dateStr.isEmpty()) {
                                    try {
                                        return sdf.parse(dateStr);
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return null;
                        },
                        Comparator.nullsLast(Date::compareTo)
                );

                pinned.sort(dateComparator);
                upcoming.sort(dateComparator);
                past.sort(dateComparator.reversed());

                if (!pinned.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Pinned Trips", "pinned", pinned.subList(0, Math.min(3, pinned.size()))));
                if (!upcoming.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Upcoming Trips", "upcoming", upcoming.subList(0, Math.min(3, upcoming.size()))));
                if (!past.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Past Trips", "past", past.subList(0, Math.min(3, past.size()))));

                recyclerView.setAdapter(new HomeCombinedAdapter(sections, sectionType -> {
                    TripCardListFragment fragment = TripCardListFragment.newInstance(sectionType);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error loading trips", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Nullable
    private Date getFirstTripStartDate(TripConfiguration trip) {
        List<Destination> destinations = trip.getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            String dateStr = destinations.get(0).getTripStartDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldResetTripConfiguration) {
            tripConfigurationViewModel.resetTripConfiguration();
            shouldResetTripConfiguration = false;
        }
    }

    private void importCitiesToFirestore() {
        try {
            InputStream is = requireContext().getAssets().open("cities_part1.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);
            Log.d("FIREBASE_city", "Total cities: " + jsonArray.length());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            WriteBatch batch = db.batch();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject cityObj = jsonArray.getJSONObject(i);

                String cityName = cityObj.getString("city");
                String cityAscii = cityObj.getString("city_ascii");
                String country = cityObj.getString("country");
                String iso2 = cityObj.getString("iso2");
                String lat = cityObj.getString("lat");
                String lng = cityObj.getString("lng");

                Map<String, Object> cityData = new HashMap<>();
                cityData.put("city", cityName);
                cityData.put("city_ascii", cityAscii);
                cityData.put("country", country);
                cityData.put("iso2", iso2);
                cityData.put("lat", lat);
                cityData.put("lng", lng);

                DocumentReference docRef = db.collection("cities").document(); // Firestore generează ID automat
                batch.set(docRef, cityData);
                Thread.sleep(20);
                if ((i + 1) % 100 == 0 || i == jsonArray.length() - 1) {
                    int current = i + 1;
                    Log.d("FIREBASE_city", "Imported cities: " + current);
                    batch.commit()
                            .addOnSuccessListener(unused -> Log.d("FIREBASE_city", "Batch of cities committed."))
                            .addOnFailureListener(e -> Log.e("FIREBASE_city", "Batch commit failed", e));
                    batch = db.batch(); // pornim un nou batch
                }
            }

        } catch (IOException | JSONException e) {
            Log.e("FIREBASE_city", "Error loading JSON or writing to Firestore", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("FIREBASE_city", "Thread sleep interrupted", e);
        }
    }

}
