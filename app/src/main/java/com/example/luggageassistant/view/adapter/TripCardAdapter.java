package com.example.luggageassistant.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.luggageassistant.utils.CountryCode;
import com.example.luggageassistant.utils.GetAllTripData;
import com.example.luggageassistant.view.TripCardListFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TRIP = 1;
    private Map<TripConfiguration, String> tripCategoryMap = new HashMap<>();

    public interface OnTripClickListener {
        void onTripClick(TripConfiguration trip);
        void onTripDelete(TripConfiguration trip, int position);
        void onTripPin(TripConfiguration trip, int position);
        void onTripUnpin(TripConfiguration trip, int position);
        void onTripLongClick(View view, TripConfiguration trip, int position);
    }

    private final List<Object> displayList = new ArrayList<>();
    private final OnTripClickListener listener;

    public TripCardAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public OnTripClickListener getListener() {
        return listener;
    }

    public void setData(List<Object> items) {
        displayList.clear();
        displayList.addAll(items);
        notifyDataSetChanged();
    }
    public void setTripCategoryMap(Map<TripConfiguration, String> categoryMap) {
        this.tripCategoryMap = categoryMap;
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
                    .inflate(R.layout.item_home_trip_section, parent, false);
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
        TextView sectionTitle;
        ImageView sectionIcon;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.section_title);
            sectionIcon = itemView.findViewById(R.id.section_icon);

        }

        void bind(String title) {
            sectionTitle.setText(title);
            if (title.equals("Upcoming Trips")) sectionIcon.setImageResource(R.drawable.ic_hourglass_top);
            else if (title.equals("Past Trips")) sectionIcon.setImageResource(R.drawable.ic_hourglass_bottom);
            else if (title.equals("Pinned Trips")) sectionIcon.setImageResource(R.drawable.ic_pin);

        }
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripDate;
        LinearLayout locationContainer;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripDate = itemView.findViewById(R.id.dateText);
            locationContainer = itemView.findViewById(R.id.locationContainer);
        }

        public void bind(TripConfiguration trip) {

            List<Destination> destinations = trip.getDestinations();

            String startDate = GetAllTripData.getEarliestStartDate(destinations);
            String endDate = GetAllTripData.getLatestEndDate(destinations);
            tripDate.setText(startDate + " - " + endDate);

            locationContainer.removeAllViews();

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTripLongClick(v, trip, getAdapterPosition());
                    return true;
                }
                return false;
            });

            Map<String, List<String>> countryToCities = new LinkedHashMap<>();
            if (destinations != null) {
                for (Destination d : destinations) {
                    if (d.getCountry() == null || d.getCity() == null) continue;
                    String country = d.getCountry();
                    String city = d.getCity();

                    if (!countryToCities.containsKey(country)) {
                        countryToCities.put(country, new ArrayList<>());
                    }
                    if (!countryToCities.get(country).contains(city)) {
                        countryToCities.get(country).add(city);
                    }
                }
            }

            for (Map.Entry<String, List<String>> entry : countryToCities.entrySet()) {
                String country = entry.getKey();
                List<String> cities = entry.getValue();
                String cityText = GetAllTripData.buildLimitedString(new LinkedHashSet<>(cities), 30);

                LinearLayout rowLayout = new LinearLayout(itemView.getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setGravity(Gravity.CENTER_VERTICAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                rowLayout.setPadding(0, 4, 0, 4);

                ImageView flagIcon = new ImageView(itemView.getContext());
                flagIcon.setLayoutParams(new LinearLayout.LayoutParams(36, 24));
                flagIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int flagResId = getFlagResIdForCountry(country);
                flagIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                flagIcon.setImageResource(flagResId);

                TextView textView = new TextView(itemView.getContext());
                textView.setText(country + " - " + cityText);
                textView.setTextSize(16);
                textView.setPadding(8, 0, 0, 0);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                ));

                rowLayout.addView(flagIcon);
                rowLayout.addView(textView);
                locationContainer.addView(rowLayout);
            }

            String category = tripCategoryMap.getOrDefault(trip, "future");

            int backgroundColorRes;
            int textColorRes;

            switch (category) {
                case "pinned":
                    backgroundColorRes = R.color.tertiary;
                    textColorRes = R.color.onTertiary;
                    break;
                case "past":
                    backgroundColorRes = R.color.surface;
                    textColorRes = R.color.onBackground;
                    break;
                default:
                    backgroundColorRes = R.color.primary;
                    textColorRes = R.color.onPrimary;
                    break;
            }

            CardView cardView = itemView.findViewById(R.id.tripCard);
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), backgroundColorRes));

            tripDate.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));

            for (int i = 0; i < locationContainer.getChildCount(); i++) {
                View child = locationContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    for (int j = 0; j < row.getChildCount(); j++) {
                        if (row.getChildAt(j) instanceof TextView) {
                            ((TextView) row.getChildAt(j)).setTextColor(
                                    ContextCompat.getColor(itemView.getContext(), textColorRes)
                            );
                        }
                    }
                }
            }
        }

        private int getFlagResIdForCountry(String countryName) {
            String iso2Code = CountryCode.getIso2Code(countryName);

            if (iso2Code == null) return R.drawable.az;

            Context context = itemView.getContext();
            int resId = context.getResources().getIdentifier(
                    iso2Code.toLowerCase(Locale.ROOT), "drawable", context.getPackageName());

            return resId != 0 ? resId : R.drawable.az;
        }
    }
}