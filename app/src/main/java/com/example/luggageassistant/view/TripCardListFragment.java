package com.example.luggageassistant.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.OnTripConfigurationsLoadedListener;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.utils.SwipeController;
import com.example.luggageassistant.view.adapter.TripCardAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripCardListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TripCardAdapter adapter;
    private final List<TripConfiguration> tripList = new ArrayList<>();
    private TripConfigurationRepository repository;
    private EditText searchEditText;
    private ProgressBar loadingSpinner;

    private final List<TripConfiguration> fullTripList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_card_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.tripRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repository = TripConfigurationRepository.getInstance();
        searchEditText = view.findViewById(R.id.searchEditText);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTrips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new TripCardAdapter(new TripCardAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripConfiguration trip) {
                SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                prefs.edit().putString("current_trip_id", trip.getTripId()).apply();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new FinalPackingListFragment())
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onTripDelete(TripConfiguration trip, int position) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                repository.deleteTripConfiguration(userId, trip.getTripId(), () -> {
                    Toast.makeText(getContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
                    fullTripList.remove(trip);
                    sortAndDisplayTrips();
                });
            }
        });

        recyclerView.setAdapter(adapter);
        loadingSpinner.setVisibility(View.VISIBLE);

        // Fetch din Firestore
        repository.getAllTripConfigurations(userId, new OnTripConfigurationsLoadedListener() {
            @Override
            public void onTripsLoaded(List<TripConfiguration> trips) {
                loadingSpinner.setVisibility(View.GONE);
                tripList.clear();
                fullTripList.clear();
                fullTripList.addAll(trips);
                sortAndDisplayTrips();
            }

            @Override
            public void onError(Exception e) {
                loadingSpinner.setVisibility(View.GONE);

                Toast.makeText(getContext(), "Error loading the trips.", Toast.LENGTH_SHORT).show();
                Log.e("TripCardList", "Firestore error", e);
            }
        });
    }

    private void filterTrips(String query) {
        tripList.clear();
        if (query.isEmpty()) {
            tripList.addAll(fullTripList);
        } else {
            for (TripConfiguration trip : fullTripList) {
                if (trip.getCountry().toLowerCase().contains(query.toLowerCase())
                        || trip.getCity().toLowerCase().contains(query.toLowerCase())
                        || trip.getTripStartDate().toLowerCase().contains(query.toLowerCase())
                        || trip.getTripEndDate().toLowerCase().contains(query.toLowerCase())) {
                    tripList.add(trip);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void sortAndDisplayTrips() {
        List<TripConfiguration> pinnedTrips = new ArrayList<>();
        List<TripConfiguration> futureTrips = new ArrayList<>();
        List<TripConfiguration> pastTrips = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (TripConfiguration trip : fullTripList) {
            if (trip.isPinned()) {
                pinnedTrips.add(trip);
                continue;
            }
            try {
                Date startDate = sdf.parse(trip.getTripStartDate());
                if (!startDate.before(today)) {
                    futureTrips.add(trip);
                } else {
                    pastTrips.add(trip);
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        // Sortează fiecare secțiune dacă vrei
        futureTrips.sort(Comparator.comparing(trip -> {
            try {
                return sdf.parse(trip.getTripStartDate());
            } catch (java.text.ParseException e) {
                return new Date(Long.MAX_VALUE);
            }
        }));

        pastTrips.sort((a, b) -> {
            try {
                return sdf.parse(b.getTripStartDate()).compareTo(sdf.parse(a.getTripStartDate()));
            } catch (java.text.ParseException e) {
                return 0;
            }
        });


        // Construiește lista finală
        List<Object> displayList = new ArrayList<>();

        if (!pinnedTrips.isEmpty()) {
            displayList.add("📌 Pinned Trips");
            displayList.addAll(pinnedTrips);
        }
        if (!futureTrips.isEmpty()) {
            displayList.add("Upcoming Trips");
            displayList.addAll(futureTrips);
        }
        if (!pastTrips.isEmpty()) {
            displayList.add("Past Trips");
            displayList.addAll(pastTrips);
        }

        adapter.setData(displayList);

    }

    public void refreshTrips() {
        sortAndDisplayTrips();
    }

}
