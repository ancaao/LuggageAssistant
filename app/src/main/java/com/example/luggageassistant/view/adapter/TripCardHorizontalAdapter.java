package com.example.luggageassistant.view.adapter;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;

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

public class TripCardHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TRIP = 0;
    private static final int VIEW_TYPE_SEE_ALL = 1;

    private final List<TripConfiguration> trips;
    private final String sectionType;
    private final HomeCombinedAdapter.OnSeeAllClickListener listener;

    public TripCardHorizontalAdapter(List<TripConfiguration> trips, String sectionType, HomeCombinedAdapter.OnSeeAllClickListener listener) {
        this.trips = new ArrayList<>(trips);
        this.sectionType = sectionType;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < trips.size()) ? VIEW_TYPE_TRIP : VIEW_TYPE_SEE_ALL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_TRIP) {
            View view = inflater.inflate(R.layout.item_home_trip_card, parent, false);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int)(parent.getResources().getDisplayMetrics().widthPixels / 2.5f);


            view.setLayoutParams(params);
            return new TripViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_home_trip_card, parent, false);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int)(parent.getResources().getDisplayMetrics().widthPixels / 2.5f);


            view.setLayoutParams(params);
            return new SeeAllViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TripViewHolder) {
            TripConfiguration trip = trips.get(position);
            TripViewHolder vh = (TripViewHolder) holder;

            Destination firstDest = null;
            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                firstDest = trip.getDestinations().get(0);
            }

            String city = "-";
            String country = "-";
            String startDate = "-";
            String endDate = "-";

            if (firstDest != null) {
                city = firstDest.getCity() != null ? firstDest.getCity() : "-";
                country = firstDest.getCountry() != null ? firstDest.getCountry() : "-";
                startDate = firstDest.getTripStartDate() != null ? firstDest.getTripStartDate() : "-";
                endDate = firstDest.getTripEndDate() != null ? firstDest.getTripEndDate() : "-";
            }

            vh.city.setText(city);
            vh.country.setText(country);
            vh.dates.setText(startDate + " - " + endDate);
            vh.purpose.setText(TextUtils.join(", ", trip.getTravelPurpose()));

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
                        vh.persons.setText(TextUtils.join(", ", names));
                    });
        } else {
            SeeAllViewHolder vh = (SeeAllViewHolder) holder;

            vh.purpose.setText("See All");
            vh.purpose.setTextSize(18);
            vh.purpose.setGravity(Gravity.CENTER);
            vh.purpose.setTypeface(null, Typeface.BOLD);
            vh.purpose.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            vh.persons.setText("→");
            vh.persons.setTextSize(18);
            vh.persons.setGravity(Gravity.CENTER);
            vh.persons.setTypeface(null, Typeface.BOLD);
            vh.persons.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // ascunde celelalte texte
            vh.country.setText("");
            vh.dates.setVisibility(View.GONE);
            vh.city.setText("");

            // elimină fundalul de la container city-country
            vh.cityCountryContainer.setBackground(null);
            vh.cityCountryContainer.setPadding(0, 0, 0, 0);

            // click
            vh.itemView.setOnClickListener(v -> listener.onSeeAllClick(sectionType));

        }
    }

    @Override
    public int getItemCount() {
        return trips.size() + 1;
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, dates, purpose, persons;
        LinearLayout cityCountryContainer;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            dates = itemView.findViewById(R.id.tv_dates);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
            cityCountryContainer = itemView.findViewById(R.id.city_country_container);

        }
    }

    static class SeeAllViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, dates, purpose, persons;
        LinearLayout cityCountryContainer;

        public SeeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            dates = itemView.findViewById(R.id.tv_dates);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
            cityCountryContainer = itemView.findViewById(R.id.city_country_container);
        }
    }
}

