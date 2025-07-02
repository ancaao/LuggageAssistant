package com.example.luggageassistant.view.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.repository.PackingListRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalPackingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> displayList = new ArrayList<>();
    private boolean deleteMode = false;
    private OnDeleteClickListener deleteClickListener;

    public FinalPackingListAdapter(Map<String, List<PackingItem>> categorizedItems) {
        for (Map.Entry<String, List<PackingItem>> entry : categorizedItems.entrySet()) {
            displayList.add(entry.getKey());
            displayList.addAll(entry.getValue());
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(PackingItem item);
    }

    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
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
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind((PackingItem) displayList.get(position), deleteMode, deleteClickListener);
        }
    }


    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void updateData(Map<String, List<PackingItem>> newData) {
        displayList.clear();
        for (Map.Entry<String, List<PackingItem>> entry : newData.entrySet()) {
            displayList.add(entry.getKey());
            displayList.addAll(entry.getValue());
        }
        notifyDataSetChanged();
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
        private final ImageView deleteIcon;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }

        public void bind(PackingItem item, boolean deleteMode, OnDeleteClickListener listener) {
            itemView.setTranslationX(0f);
            itemView.setAlpha(1f);

            if (item.getQuantity() > 1) {
                itemCheckBox.setText(item.getItem() + " - " + item.getQuantity());
            } else {
                itemCheckBox.setText(item.getItem());
            }

            itemCheckBox.setOnCheckedChangeListener(null);
            itemCheckBox.setChecked(item.isChecked());
            updateStyle(item.isChecked());

            itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                updateStyle(isChecked);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Context context = itemCheckBox.getContext();
                String tripId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .getString("current_trip_id", null);

                if (tripId != null && item.getPersonName() != null) {
                    PackingListRepository.getInstance()
                            .updateFinalItemChecked(userId, tripId, item.getPersonName(), item, isChecked);
                } else {
                    Log.e("CHECKBOX_UPDATE", "Trip ID sau personName este null");
                }
            });

            deleteIcon.setVisibility(deleteMode ? View.VISIBLE : View.GONE);
            deleteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    itemView.animate()
                            .translationX(-itemView.getWidth())
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                listener.onDeleteClick(item);
                            })
                            .start();
                }
            });


            itemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                updateStyle(isChecked);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Context context = itemCheckBox.getContext();
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
    public void removeItem(PackingItem item) {
        displayList.remove(item);
        notifyDataSetChanged();
    }

    public void restoreItem(PackingItem item) {
        List<PackingItem> singleItemList = new ArrayList<>();
        for (Object obj : displayList) {
            if (obj instanceof PackingItem) {
                singleItemList.add((PackingItem) obj);
            }
        }
        singleItemList.add(item);
        updateData(categorizeItems(singleItemList));
    }
    public static Map<String, List<PackingItem>> categorizeItems(List<PackingItem> items) {
        Map<String, List<PackingItem>> categorized = new LinkedHashMap<>();
        for (PackingItem item : items) {
            String category = item.getCategory();
            if (!categorized.containsKey(category)) {
                categorized.put(category, new ArrayList<>());
            }
            categorized.get(category).add(item);
        }
        return categorized;
    }
}
