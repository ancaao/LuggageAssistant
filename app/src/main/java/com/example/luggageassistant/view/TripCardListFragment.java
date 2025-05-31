package com.example.luggageassistant.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.OnTripConfigurationsLoadedListener;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.utils.CountryCode;
import com.example.luggageassistant.view.adapter.TripCardAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripCardListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TripCardAdapter adapter;
    private final List<TripConfiguration> tripList = new ArrayList<>();
    private TripConfigurationRepository repository;
    private EditText searchEditText;
    private ProgressBar loadingSpinner;
    private final Map<TripConfiguration, String> tripCategoryMap = new HashMap<>();


    private final List<TripConfiguration> fullTripList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_card_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CountryCode.init(requireContext());

        recyclerView = view.findViewById(R.id.tripRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repository = TripConfigurationRepository.getInstance();
        searchEditText = view.findViewById(R.id.searchEditText);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String section = getArguments() != null ? getArguments().getString("section_type") : null;

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

            @Override
            public void onTripPin(TripConfiguration trip, int position) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                repository.updateTripPinned(userId, trip.getTripId(), true, () -> {
                    trip.setPinned(true); // update local
                    sortAndDisplayTrips();
                });
            }

            @Override
            public void onTripUnpin(TripConfiguration trip, int position) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                repository.updateTripPinned(userId, trip.getTripId(), false, () -> {
                    trip.setPinned(false); // update local
                    sortAndDisplayTrips();
                });
            }

            @Override
            public void onTripLongClick(View view, TripConfiguration trip, int position) {
                showPopupMenu(view, trip, position);
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

                if (section != null) {
                    int scrollPosition = findSectionStartIndex(section);
                    if (scrollPosition != -1) {
                        recyclerView.scrollToPosition(scrollPosition);
                    }
                }
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
        List<TripConfiguration> filteredTrips = new ArrayList<>();
        String lowerQuery = query.toLowerCase(Locale.ROOT);

        for (TripConfiguration trip : fullTripList) {
            Destination firstDest = trip.getFirstDestination();
            if (firstDest == null) continue;

            String city = firstDest.getCity() != null ? firstDest.getCity().toLowerCase() : "";
            String country = firstDest.getCountry() != null ? firstDest.getCountry().toLowerCase() : "";
            String startDate = firstDest.getTripStartDate() != null ? firstDest.getTripStartDate().toLowerCase() : "";
            String endDate = firstDest.getTripEndDate() != null ? firstDest.getTripEndDate().toLowerCase() : "";

            if (query.isEmpty() ||
                    city.contains(lowerQuery)
                    || country.contains(lowerQuery)
                    || startDate.contains(lowerQuery)
                    || endDate.contains(lowerQuery)) {
                filteredTrips.add(trip);
            }
        }

        List<TripConfiguration> pinnedTrips = new ArrayList<>();
        List<TripConfiguration> futureTrips = new ArrayList<>();
        List<TripConfiguration> pastTrips = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (TripConfiguration trip : filteredTrips) {
            Destination firstDest = trip.getFirstDestination();
            if (firstDest == null || firstDest.getTripStartDate() == null) continue;

            if (trip.isPinned()) {
                pinnedTrips.add(trip);
                continue;
            }

            try {
                Date startDate = sdf.parse(firstDest.getTripStartDate());
                if (!startDate.before(today)) {
                    futureTrips.add(trip);
                } else {
                    pastTrips.add(trip);
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        // sortează și grupează
        futureTrips.sort(Comparator.comparing(trip -> {
            try {
                return sdf.parse(trip.getFirstDestination().getTripStartDate());
            } catch (Exception e) {
                return new Date(Long.MAX_VALUE);
            }
        }));

        pastTrips.sort((a, b) -> {
            try {
                return sdf.parse(b.getFirstDestination().getTripStartDate())
                        .compareTo(sdf.parse(a.getFirstDestination().getTripStartDate()));
            } catch (Exception e) {
                return 0;
            }
        });

        List<Object> displayList = new ArrayList<>();
        tripCategoryMap.clear();
        for (TripConfiguration trip : pinnedTrips) {
            tripCategoryMap.put(trip, "pinned");
        }
        for (TripConfiguration trip : futureTrips) {
            tripCategoryMap.put(trip, "future");
        }
        for (TripConfiguration trip : pastTrips) {
            tripCategoryMap.put(trip, "past");
        }

        if (!pinnedTrips.isEmpty()) {
            displayList.add("Pinned Trips");
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

        adapter.setTripCategoryMap(tripCategoryMap);
        adapter.setData(displayList);
    }


    private void showPopupMenu(View anchorView, TripConfiguration trip, int position) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(
                trip.isPinned() ? R.menu.trip_card_popup_menu_unpin : R.menu.trip_card_popup_menu,
                popupMenu.getMenu()
        );

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_pin) {
                adapter.getListener().onTripPin(trip, position);
                return true;
            } else if (itemId == R.id.menu_unpin) {
                adapter.getListener().onTripUnpin(trip, position);
                return true;
            } else if (itemId == R.id.menu_delete) {
                adapter.getListener().onTripDelete(trip, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }


    private void sortAndDisplayTrips() {
        List<TripConfiguration> pinnedTrips = new ArrayList<>();
        List<TripConfiguration> futureTrips = new ArrayList<>();
        List<TripConfiguration> pastTrips = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (TripConfiguration trip : fullTripList) {
            Destination firstDest = trip.getFirstDestination();
            if (firstDest == null || firstDest.getTripStartDate() == null) continue;

            if (trip.isPinned()) {
                pinnedTrips.add(trip);
                continue;
            }

            try {
                Date startDate = sdf.parse(firstDest.getTripStartDate());
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
                return sdf.parse(trip.getFirstDestination().getTripStartDate());
            } catch (java.text.ParseException | NullPointerException e) {
                return new Date(Long.MAX_VALUE);
            }
        }));

        pastTrips.sort((a, b) -> {
            try {
                return sdf.parse(b.getFirstDestination().getTripStartDate())
                        .compareTo(sdf.parse(a.getFirstDestination().getTripStartDate()));
            } catch (java.text.ParseException | NullPointerException e) {
                return 0;
            }
        });

        // Construiește lista finală
        List<Object> displayList = new ArrayList<>();
        tripCategoryMap.clear();
        for (TripConfiguration trip : pinnedTrips) {
            tripCategoryMap.put(trip, "pinned");
        }
        for (TripConfiguration trip : futureTrips) {
            tripCategoryMap.put(trip, "future");
        }
        for (TripConfiguration trip : pastTrips) {
            tripCategoryMap.put(trip, "past");
        }

        adapter.setTripCategoryMap(tripCategoryMap);
        adapter.setData(displayList);

        if (!pinnedTrips.isEmpty()) {
            displayList.add("Pinned Trips");
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

    public static TripCardListFragment newInstance(String sectionType) {
        TripCardListFragment fragment = new TripCardListFragment();
        Bundle args = new Bundle();
        args.putString("section_type", sectionType);
        fragment.setArguments(args);
        return fragment;
    }
    private int findSectionStartIndex(String sectionType) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Object item = adapter.getItemAt(i); // adaptează în funcție de implementarea ta
            if (item instanceof String && ((String) item).toLowerCase().contains(sectionType)) {
                return i;
            }
        }
        return -1;
    }
}
