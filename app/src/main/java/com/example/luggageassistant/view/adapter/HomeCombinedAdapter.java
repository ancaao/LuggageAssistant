package com.example.luggageassistant.view.adapter;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeCombinedAdapter extends RecyclerView.Adapter<HomeCombinedAdapter.SectionViewHolder> {

    public interface OnSeeAllClickListener {
        void onSeeAllClick(String sectionType);
    }

    private final List<TripSection> sections;
    private final OnSeeAllClickListener listener;

    public HomeCombinedAdapter(List<TripSection> sections, OnSeeAllClickListener listener) {
        this.sections = sections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_trip_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        TripSection section = sections.get(position);
        holder.title.setText(section.getTitle());

        TripCardHorizontalAdapter adapter = new TripCardHorizontalAdapter(section.getTrips(), section.getType(), listener);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RecyclerView recyclerView;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.section_title);
            recyclerView = itemView.findViewById(R.id.section_recycler);
        }
    }

    public static class TripSection {
        private final String title;
        private final String type;
        private final List<TripConfiguration> trips;

        public TripSection(String title, String type, List<TripConfiguration> trips) {
            this.title = title;
            this.type = type;
            this.trips = trips;
        }

        public String getTitle() { return title; }
        public String getType() { return type; }
        public List<TripConfiguration> getTrips() { return trips; }
    }
}
