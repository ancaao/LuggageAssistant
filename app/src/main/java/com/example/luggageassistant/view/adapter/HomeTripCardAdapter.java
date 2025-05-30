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
import com.example.luggageassistant.utils.GetAllTripData;
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

        List<Destination> destinations = trip.getDestinations() != null ? trip.getDestinations() : new ArrayList<>();

        holder.city.setText(GetAllTripData.getAllCities(destinations));
        holder.country.setText(GetAllTripData.getAllCountries(destinations));
        holder.startDate.setText(GetAllTripData.getEarliestStartDate(destinations));
        holder.endDate.setText(GetAllTripData.getLatestEndDate(destinations));

        holder.purpose.setText(TextUtils.join(", ", trip.getTravelPurpose()));

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
        TextView city, country, startDate, endDate, purpose, persons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            startDate = itemView.findViewById(R.id.tv_start_date);
            endDate = itemView.findViewById(R.id.tv_end_date);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
        }

    }
}
