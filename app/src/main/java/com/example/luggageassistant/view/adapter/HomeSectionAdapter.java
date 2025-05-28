package com.example.luggageassistant.view.adapter;

import android.graphics.Typeface;
import android.text.TextUtils;
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

public class HomeSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_TRIP = 1;

    private final List<Object> items;

    public HomeSectionAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? VIEW_TYPE_HEADER : VIEW_TYPE_TRIP;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_home_trip_card, parent, false);
            return new TripViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText((String) items.get(position));
        } else if (holder instanceof TripViewHolder) {
            TripConfiguration trip = (TripConfiguration) items.get(position);
            TripViewHolder tripHolder = (TripViewHolder) holder;

            // Obține prima destinație
            Destination firstDest = null;
            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                firstDest = trip.getDestinations().get(0);
            }

            // Afișează datele din prima destinație
            if (firstDest != null) {
                tripHolder.city.setText(firstDest.getCity() != null ? firstDest.getCity() : "-");
                tripHolder.country.setText(firstDest.getCountry() != null ? firstDest.getCountry() : "-");

                String start = firstDest.getTripStartDate() != null ? firstDest.getTripStartDate() : "-";
                String end = firstDest.getTripEndDate() != null ? firstDest.getTripEndDate() : "-";
                tripHolder.dates.setText(start + " - " + end);
            } else {
                tripHolder.city.setText("-");
                tripHolder.country.setText("-");
                tripHolder.dates.setText("-");
            }

            // Afișează scopul călătoriei
            tripHolder.purpose.setText(TextUtils.join(", ", trip.getTravelPurpose()));

            // Afișează numele persoanelor din lista finală
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
                        tripHolder.persons.setText(TextUtils.join(", ", names));
                    });
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            title.setTextSize(18);
            title.setTypeface(null, Typeface.BOLD);
        }
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, dates, purpose, persons;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            dates = itemView.findViewById(R.id.tv_dates);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
        }
    }
}
