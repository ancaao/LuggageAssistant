package com.example.luggageassistant.view.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeTripCardAdapter extends RecyclerView.Adapter<HomeTripCardAdapter.ViewHolder> {

    private final List<TripConfiguration> trips;

    public HomeTripCardAdapter(List<TripConfiguration> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_trip_card, parent, false);
        Log.d("DEBUG_HOME", "Adapter received " + trips.size() + " items");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripConfiguration trip = trips.get(position);

        // Folosește prima destinație dacă există
        Destination firstDest = null;
        if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
            firstDest = trip.getDestinations().get(0);
        }

        if (firstDest != null) {
            holder.city.setText(firstDest.getCity() != null ? firstDest.getCity() : "-");
            holder.country.setText(firstDest.getCountry() != null ? firstDest.getCountry() : "-");

            String start = firstDest.getTripStartDate() != null ? firstDest.getTripStartDate() : "-";
            String end = firstDest.getTripEndDate() != null ? firstDest.getTripEndDate() : "-";
            holder.dates.setText(start + " - " + end);
        } else {
            holder.city.setText("-");
            holder.country.setText("-");
            holder.dates.setText("-");
        }

        // Afișează scopul călătoriei
        holder.purpose.setText(TextUtils.join(", ", trip.getTravelPurpose()));

        // Load persons from finalLists
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("trips")
                .document(trip.getTripId())
                .collection("finalLists")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> names = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        names.add(doc.getId());
                    }
                    holder.persons.setText(TextUtils.join(", ", names));
                });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, dates, purpose, persons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            dates = itemView.findViewById(R.id.tv_dates);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
        }
    }
}
