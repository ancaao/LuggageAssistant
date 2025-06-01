package com.example.luggageassistant.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.utils.GetAllTripData;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_TRIP = "arg_trip";
    private List<TripConfiguration> trips;

    public static CalendarBottomSheetDialogFragment newInstance(List<Pair<TripConfiguration, Destination>> data, String clickedDate) {
        CalendarBottomSheetDialogFragment fragment = new CalendarBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("arg_pairs", new ArrayList<>(data));
        args.putString("clicked_date", clickedDate);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calendar_bottom_sheet, container, false);

        LinearLayout containerLayout = view.findViewById(R.id.tripsContainer);

        List<Pair<TripConfiguration, Destination>> pairs = null;

        if (getArguments() != null) {
            pairs = (List<Pair<TripConfiguration, Destination>>) getArguments().getSerializable("arg_pairs");
        }

        if (pairs != null && !pairs.isEmpty()) {
            for (Pair<TripConfiguration, Destination> pair : pairs) {
                TripConfiguration trip = pair.first;
                Destination destination = pair.second;

                View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_calendar_trip_card, containerLayout, false);

                LinearLayout locationContainer = cardView.findViewById(R.id.locationContainer);
                MaterialButton btnPackingList = cardView.findViewById(R.id.btnPackingList);

                TextView locationText = new TextView(getContext());
                locationText.setText(destination.getCity() + " - " + destination.getCountry());
                locationText.setTextSize(14);
                locationText.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                locationText.setPadding(0, 0, 0, 0);

                locationContainer.addView(locationText);

                btnPackingList.setOnClickListener(v -> {
                    Context context = requireContext();
                    SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("current_trip_id", trip.getTripId()).apply();

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new FinalPackingListFragment())
                            .addToBackStack(null)
                            .commit();

                    // opțional: închide bottom sheet-ul
                    dismiss();

                });

                containerLayout.addView(cardView);
            }
        } else {
            TextView fallbackText = new TextView(requireContext());
            fallbackText.setText("No trips available.");
            fallbackText.setTextSize(16);
            fallbackText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.secondary_text_dark));
            containerLayout.addView(fallbackText);
        }

        return view;
    }
}
