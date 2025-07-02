package com.example.luggageassistant.view;

import android.animation.ValueAnimator;
import android.net.ParseException;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.OnItemsLoadedListener;
import com.example.luggageassistant.repository.OnTripConfigurationsLoadedListener;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.utils.GetAllTripData;
import com.example.luggageassistant.utils.WeatherCacheHelper;
import com.example.luggageassistant.utils.WeatherCardHelper;
import com.example.luggageassistant.view.adapter.HomeCombinedAdapter;
import com.example.luggageassistant.viewmodel.MainViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.example.luggageassistant.viewmodel.WeatherViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView textView;
    private boolean shouldResetTripConfiguration = false;
    private List<TripConfiguration> upcomingTrips = new ArrayList<>();
    private CardView weatherCard;
    private TextView weatherTemperature, weatherCardTitle;
    private boolean shortTermDone = false;
    private boolean longTermDone = false;
    private boolean shortTermRequired = false;
    private boolean longTermRequired = false;
    private String currentTripIdForWeather;
    private View homeLoadingLayout;
    private View homeContentLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeLoadingLayout = view.findViewById(R.id.home_loading_layout);
        homeContentLayout = view.findViewById(R.id.home_content);

        homeLoadingLayout.setVisibility(View.VISIBLE);
        homeContentLayout.setVisibility(View.GONE);

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


        weatherCard = view.findViewById(R.id.weather_card);
        weatherCardTitle = view.findViewById(R.id.weather_card_title);
        weatherTemperature = view.findViewById(R.id.weather_temperature);


        WeatherViewModel viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        viewModel.getForecastLiveData().observe(getViewLifecycleOwner(), forecastList -> {
            if (upcomingTrips.isEmpty()) return;
            TripConfiguration trip = upcomingTrips.get(0);
            WeatherCardHelper.processForecastList(
                    forecastList,
                    trip,
                    weatherCard,
                    weatherCardTitle,
                    weatherTemperature
            );
            shortTermDone = true;
            maybeShowWeatherCard();
        });

        viewModel.getCoordinatesResult().observe(getViewLifecycleOwner(), pair -> {
            if (pair == null) return;
            String coordStr = pair.first;
            Destination destination = pair.second;

            WeatherCardHelper.handleCoordinatesResult(
                    coordStr,
                    destination,
                    viewModel
            );
        });

        viewModel.getLongTermForecastJson().observe(getViewLifecycleOwner(), json -> {
            if (upcomingTrips.isEmpty()) return;
            TripConfiguration trip = upcomingTrips.get(0);

            if (json != null && json.contains("|")) {
                String[] parts = json.split("\\|", 2);
                String date = parts[0];
                String body = parts[1];
                WeatherCardHelper.processLongTermJson(
                        body,
                        trip,
                        weatherCard,
                        weatherCardTitle,
                        weatherTemperature,
                        date);
            }
            longTermDone = true;
            maybeShowWeatherCard();
        });

        RecyclerView recyclerView = view.findViewById(R.id.home_combined_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TripConfigurationRepository.getInstance().getAllTripConfigurations(userId, new OnTripConfigurationsLoadedListener() {
            @Override
            public void onTripsLoaded(List<TripConfiguration> trips) {
                homeLoadingLayout.setVisibility(View.GONE);
                homeContentLayout.setVisibility(View.VISIBLE);

                List<HomeCombinedAdapter.TripSection> sections = new ArrayList<>();

                GetAllTripData.CategorizedTrips categorized = GetAllTripData.categorizeTrips(trips);
                List<TripConfiguration> pinned = categorized.pinned;
                List<TripConfiguration> upcoming = categorized.upcoming;
                List<TripConfiguration> past = categorized.past;

                List<TripConfiguration> pinnedFuture = new ArrayList<>();
                Date now = new Date();
                for (TripConfiguration trip : pinned) {
                    if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                        Destination lastDestination = trip.getDestinations().get(trip.getDestinations().size() - 1);
                        try {
                            Date endDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(lastDestination.getTripEndDate());
                            if (endDate != null && endDate.after(now)) {
                                pinnedFuture.add(trip);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<TripConfiguration> weatherRelevantTrips = new ArrayList<>(upcoming);
                for (TripConfiguration pinnedTrip : pinnedFuture) {
                    if (!weatherRelevantTrips.contains(pinnedTrip)) {
                        weatherRelevantTrips.add(pinnedTrip);
                    }
                }

                Collections.sort(weatherRelevantTrips, (t1, t2) -> {
                    try {
                        String d1 = t1.getDestinations().get(0).getTripStartDate();
                        String d2 = t2.getDestinations().get(0).getTripStartDate();
                        Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(d1);
                        Date date2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(d2);
                        return date1.compareTo(date2);
                    } catch (Exception e) {
                        return 0;
                    }
                });


                if (!weatherRelevantTrips.isEmpty()) {

                    if (!isAdded()) return;
                    HomeFragment.this.upcomingTrips = weatherRelevantTrips;

                    LinearProgressIndicator progressBar = view.findViewById(R.id.packing_progress_bar);
                    TextView progressText = view.findViewById(R.id.packing_progress_text);
                    progressText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    TripConfiguration nextTrip = weatherRelevantTrips.get(0);
                    String tripId = nextTrip.getTripId();

                    shortTermRequired = false;
                    longTermRequired = false;

                    List<Destination> destinations = nextTrip.getDestinations();
                    Date today = new Date();
                    long todayMillis = today.getTime();

                    for (Destination dest : destinations) {
                        List<String> dateRange = WeatherCardHelper.generateDateRange(dest.getTripStartDate(), dest.getTripEndDate());

                        for (String dateStr : dateRange) {
                            try {
                                Date day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                                long diffDays = (day.getTime() - todayMillis) / (1000 * 60 * 60 * 24);

                                if (diffDays < 0 || diffDays > 500) continue;

                                if (diffDays <= 14) shortTermRequired = true;
                                else longTermRequired = true;

                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    PackingListRepository.getInstance().getFinalPackingItems(userId, tripId, new OnItemsLoadedListener() {
                        @Override
                        public void onItemsLoaded(List<PackingItem> items) {
                            if (items.isEmpty()) {
                                progressBar.setProgress(0);
                                return;
                            }

                            int checkedCount = 0;
                            for (PackingItem item : items) {
                                if (item.isChecked()) {
                                    checkedCount++;
                                }
                            }

                            int percent = (int) ((checkedCount * 100.0f) / items.size());
                            ValueAnimator animator = ValueAnimator.ofInt(0, percent);
                            animator.setDuration(500);
                            animator.addUpdateListener(animation -> {
                                int animatedValue = (int) animation.getAnimatedValue();
                                progressBar.setProgress(animatedValue);
                            });
                            progressText.setText("Packing completed: " + percent + "%");
                            animator.start();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("HomeFragment", "Error loading packing items", e);
                        }
                    });

                    if (WeatherCacheHelper.isValidForTodayTrip(requireContext(), tripId)) {
                        float minTemp = WeatherCacheHelper.getMinTemp(requireContext());
                        float maxTemp = WeatherCacheHelper.getMaxTemp(requireContext());
                        List<String> cities = WeatherCacheHelper.getCities(requireContext());

                        WeatherCardHelper.displayCachedForecast(
                                weatherCard,
                                weatherCardTitle,
                                weatherTemperature,
                                cities,
                                minTemp,
                                maxTemp
                        );
                    } else {
                        weatherCard.setVisibility(View.GONE);
                        WeatherCacheHelper.clearCache(requireContext());
                        WeatherCardHelper.resetGlobalWeatherState();

                        view.post(() -> WeatherCardHelper.processAndDisplayAggregatedWeather(
                                weatherCard,
                                weatherCardTitle,
                                weatherTemperature,
                                upcomingTrips.get(0),
                                viewModel,
                                getViewLifecycleOwner()
                        ));
                    }
                }else {
                    weatherCard.setVisibility(View.GONE);
                }


                if (!pinned.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Pinned Trips", "pinned", pinned.subList(0, Math.min(3, pinned.size()))));
                if (!upcoming.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Upcoming Trips", "upcoming", upcoming.subList(0, Math.min(3, upcoming.size()))));
                if (!past.isEmpty())
                    sections.add(new HomeCombinedAdapter.TripSection("Past Trips", "past", past.subList(0, Math.min(3, past.size()))));

                recyclerView.setAdapter(new HomeCombinedAdapter(
                        sections,
                        sectionType -> {
                            TripCardListFragment fragment = TripCardListFragment.newInstance(sectionType);
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        },
                        trip -> {
                            FinalPackingListFragment fragment = FinalPackingListFragment.newInstance(trip.getTripId());
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                ));

            }

            @Override
            public void onError(Exception e) {
                homeLoadingLayout.setVisibility(View.GONE);
                homeContentLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Error loading trips", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void maybeShowWeatherCard() {
        Log.d("WEATHER_CHECK", "maybeShowWeatherCard(): shortTermDone=" + shortTermDone +
                ", longTermDone=" + longTermDone +
                ", required: short=" + shortTermRequired + ", long=" + longTermRequired);


        boolean shortOk = !shortTermRequired || shortTermDone;
        boolean longOk = !longTermRequired || longTermDone;

        if (shortOk && longOk) {
            WeatherCardHelper.finalizeAndDisplayWeatherCard(
                    weatherCard,
                    weatherCardTitle,
                    weatherTemperature,
                    upcomingTrips.get(0).getTripId()
            );
            shortTermDone = false;
            longTermDone = false;
            shortTermRequired = false;
            longTermRequired = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldResetTripConfiguration) {
            tripConfigurationViewModel.resetTripConfiguration();
            shouldResetTripConfiguration = false;
        }
    }
}
