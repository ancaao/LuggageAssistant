package com.example.luggageassistant.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.view.adapter.CalendarAdapter;
import com.example.luggageassistant.viewmodel.CalendarViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private TextView tvMonthYear;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private final Map<LocalDate, TripConfiguration> vacationDaysMap = new HashMap<>();
    private final List<TripConfiguration> allTrips = new ArrayList<>();
    @SuppressLint("NewApi")
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private CalendarViewModel calendarViewModel;


    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initWidgets(view);

        calendarViewModel.getTripsLiveData().observe(getViewLifecycleOwner(), trips -> {
            // La încărcarea datelor din Firebase, setează calendarul
            setMonthView();
        });

        selectedDate = LocalDate.now();
        setMonthView();
        loadTripsFromFirestore();
        return view;
    }
    @SuppressLint("NewApi")
    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);

        view.findViewById(R.id.btnPreviousMonth).setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            setMonthView();
        });

        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedDate = selectedDate.plusMonths(1);
            setMonthView();
        });
    }

    private void setMonthView() {
        tvMonthYear.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, selectedDate, calendarViewModel.getVacationDaysMap());

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);
    }
    @SuppressLint("NewApi")
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }

        return daysInMonthArray;
    }

    @SuppressLint("NewApi")
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.isEmpty()) {
            int day = Integer.parseInt(dayText);
            LocalDate clickedDate = selectedDate.withDayOfMonth(day);
            String clickedDateStr = clickedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            List<Pair<TripConfiguration, Destination>> matchingDestinations = calendarViewModel.getDestinationsForDate(clickedDate);
            if (!matchingDestinations.isEmpty()) {
                CalendarBottomSheetDialogFragment fragment = CalendarBottomSheetDialogFragment.newInstance(matchingDestinations, clickedDateStr);
                fragment.show(getParentFragmentManager(), "calendarBottomSheet");
            }

            selectedDate = clickedDate;
            CalendarAdapter adapter = (CalendarAdapter) calendarRecyclerView.getAdapter();
            if (adapter != null) adapter.setSelectedDate(selectedDate);
        }
    }

    private void loadTripsFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        calendarViewModel.loadTrips(userId);
    }
}



