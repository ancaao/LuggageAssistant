package com.example.luggageassistant.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.view.TripCardListFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRIP = 1;

    public interface OnTripClickListener {
        void onTripClick(TripConfiguration trip);

        void onTripDelete(TripConfiguration trip, int position);
    }

    private final List<Object> displayList = new ArrayList<>();
    private final OnTripClickListener listener;

    public TripCardAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Object> items) {
        displayList.clear();
        displayList.addAll(items);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return (displayList.get(position) instanceof String) ? TYPE_HEADER : TYPE_TRIP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trip_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trip_card, parent, false);
            return new TripViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) displayList.get(position));
        } else {
            ((TripViewHolder) holder).bind((TripConfiguration) displayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public Object getItemAt(int position) {
        if (position < 0 || position >= displayList.size()) return null;
        return displayList.get(position);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        void bind(String title) {
            headerText.setText(title);
        }
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripTitle, tripDate;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripTitle = itemView.findViewById(R.id.titleText);
            tripDate = itemView.findViewById(R.id.dateText);
        }
        public void bind(TripConfiguration trip) {
            // Extrage prima destinație
            Destination firstDest = null;
            if (trip.getDestinations() != null && !trip.getDestinations().isEmpty()) {
                firstDest = trip.getDestinations().get(0);
            }

            String city = "-";
            String country = "-";
            String startDateStr = "-";
            String endDateStr = "-";

            if (firstDest != null) {
                city = firstDest.getCity() != null ? firstDest.getCity() : "-";
                country = firstDest.getCountry() != null ? firstDest.getCountry() : "-";
                startDateStr = firstDest.getTripStartDate() != null ? firstDest.getTripStartDate() : "-";
                endDateStr = firstDest.getTripEndDate() != null ? firstDest.getTripEndDate() : "-";
            }

            tripTitle.setText(country + " - " + city);
            tripDate.setText(startDateStr + " - " + endDateStr);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate = sdf.parse(startDateStr);
                Date today = new Date();
                boolean isUpcoming = startDate != null && !startDate.before(today);

                Context context = itemView.getContext();

                int backgroundColor = ContextCompat.getColor(context,
                        isUpcoming ? R.color.primary : R.color.surface);

                int textColor = ContextCompat.getColor(context,
                        isUpcoming ? R.color.onPrimary : R.color.onBackground);

                CardView cardView = itemView.findViewById(R.id.tripCard);
                cardView.setCardBackgroundColor(backgroundColor);

                tripTitle.setTextColor(textColor);
                tripDate.setTextColor(textColor);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            itemView.setOnClickListener(v -> listener.onTripClick(trip));

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return false;

                PopupMenu popup = new PopupMenu(itemView.getContext(), v);
                if (trip.isPinned()) {
                    popup.inflate(R.menu.trip_card_popup_menu_unpin);
                } else {
                    popup.inflate(R.menu.trip_card_popup_menu);
                }
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_pin || itemId == R.id.menu_unpin) {
                        boolean newPinnedState = itemId == R.id.menu_pin;
                        trip.setPinned(newPinnedState);

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        TripConfigurationRepository.getInstance().updateTripPinned(userId, trip.getTripId(), newPinnedState, () -> {
                            Log.d("TripCardAdapter", "Pinned updated for trip " + trip.getTripId());
                            ((TripCardListFragment) ((FragmentActivity) itemView.getContext())
                                    .getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container)).refreshTrips();
                        });
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        listener.onTripDelete(trip, position);
                        return true;
                    }
                    return false;
                });

                popup.show();
                return true;
            });
        }

    }
}