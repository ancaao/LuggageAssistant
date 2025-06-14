package com.example.luggageassistant.view.adapter;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;

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

public class TripCardHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TRIP = 0;
    private static final int VIEW_TYPE_SEE_ALL = 1;

    private final List<TripConfiguration> trips;
    private final String sectionType;
    private final HomeCombinedAdapter.OnSeeAllClickListener listener;
    private final OnTripCardClickListener tripCardClickListener;

    public TripCardHorizontalAdapter(List<TripConfiguration> trips, String sectionType,
                                     HomeCombinedAdapter.OnSeeAllClickListener listener,
                                     OnTripCardClickListener tripCardClickListener) {
        this.trips = new ArrayList<>(trips);
        this.sectionType = sectionType;
        this.listener = listener;
        this.tripCardClickListener = tripCardClickListener;
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
            params.width = (int) (parent.getResources().getDisplayMetrics().widthPixels / 2.5f);


            view.setLayoutParams(params);
            return new TripViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_home_trip_card, parent, false);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (parent.getResources().getDisplayMetrics().widthPixels / 2.5f);


            view.setLayoutParams(params);
            return new SeeAllViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TripViewHolder) {
            TripConfiguration trip = trips.get(position);
            TripViewHolder vh = (TripViewHolder) holder;

            List<Destination> destinations = trip.getDestinations();

            // ✅ înlocuiește logica veche cu metode din TripUtils
            String city = GetAllTripData.getAllCities(destinations);
            String country = GetAllTripData.getAllCountries(destinations);
            String startDate = GetAllTripData.getEarliestStartDate(destinations);
            String endDate = GetAllTripData.getLatestEndDate(destinations);

            vh.city.setText(city);
            vh.country.setText(country);
            vh.startDate.setText(startDate);
            vh.endDate.setText(endDate);

            vh.purpose.setText(TextUtils.join(", ", trip.getTravelPurpose()));

            // ✅ păstrăm afișarea personelor din Firebase
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

            vh.itemView.setOnClickListener(v -> tripCardClickListener.onTripCardClick(trip));

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

            vh.country.setText("");
            vh.startDate.setText("");
            vh.endDate.setText("");
            vh.city.setText("");

            vh.cityCountryContainer.setBackground(null);
            vh.cityCountryContainer.setPadding(0, 0, 0, 0);

            vh.iconPurpose.setVisibility(View.GONE);
            vh.iconPersons.setVisibility(View.GONE);
            vh.iconStartDate.setVisibility(View.GONE);
            vh.iconEndDate.setVisibility(View.GONE);

            vh.itemView.setOnClickListener(v -> listener.onSeeAllClick(sectionType));
        }
    }

    @Override
    public int getItemCount() {
        return trips.size() + 1;
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, startDate, endDate, purpose, persons;
        LinearLayout cityCountryContainer;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            startDate = itemView.findViewById(R.id.tv_start_date);
            endDate = itemView.findViewById(R.id.tv_end_date);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
            cityCountryContainer = itemView.findViewById(R.id.city_country_container);

        }
    }

    static class SeeAllViewHolder extends RecyclerView.ViewHolder {
        TextView city, country, startDate, endDate, purpose, persons;
        LinearLayout cityCountryContainer;
        ImageView iconPurpose, iconPersons, iconStartDate, iconEndDate;

        public SeeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_city);
            country = itemView.findViewById(R.id.tv_country);
            startDate = itemView.findViewById(R.id.tv_start_date);
            endDate = itemView.findViewById(R.id.tv_end_date);
            purpose = itemView.findViewById(R.id.tv_purpose);
            persons = itemView.findViewById(R.id.tv_persons);
            cityCountryContainer = itemView.findViewById(R.id.city_country_container);

            iconPurpose = itemView.findViewById(R.id.icon_purpose);
            iconPersons = itemView.findViewById(R.id.icon_persons);
            iconStartDate = itemView.findViewById(R.id.icon_start_date);
            iconEndDate = itemView.findViewById(R.id.icon_end_date);
        }
    }

    public interface OnTripCardClickListener {
        void onTripCardClick(TripConfiguration trip);
    }
}