package com.example.luggageassistant.view.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.repository.PackingListRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinalPackingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> displayList = new ArrayList<>();

    public FinalPackingListAdapter(Map<String, List<PackingItem>> categorizedItems) {
        for (Map.Entry<String, List<PackingItem>> entry : categorizedItems.entrySet()) {
            displayList.add(entry.getKey()); // categoria
            displayList.addAll(entry.getValue()); // itemele din acea categorie
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (displayList.get(position) instanceof String) ? TYPE_CATEGORY : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_packing_category, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_packing_checkbox, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind((String) displayList.get(position));
        } else {
            ((ItemViewHolder) holder).bind((PackingItem) displayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryTitle;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
        }

        void bind(String category) {
            categoryTitle.setText(category);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox itemCheckBox;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
        }

        public void bind(PackingItem item) {
            itemCheckBox.setText(item.getItem());
            itemCheckBox.setChecked(item.isChecked());
            updateStyle(item.isChecked());

            itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                updateStyle(isChecked);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // ✅ obține tripId din SharedPreferences
                Context context = itemCheckBox.getContext(); // orice view are context
                String tripId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .getString("current_trip_id", null);

                if (tripId != null && item.getPersonName() != null) {
                    PackingListRepository.getInstance()
                            .updateFinalItemChecked(userId, tripId, item.getPersonName(), item, isChecked);
                } else {
                    Log.e("CHECKBOX_UPDATE", "Trip ID sau personName este null");
                }
            });
        }


        void updateStyle(boolean isChecked) {
            if (isChecked) {
                itemCheckBox.setPaintFlags(itemCheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                itemCheckBox.setPaintFlags(itemCheckBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }
}
