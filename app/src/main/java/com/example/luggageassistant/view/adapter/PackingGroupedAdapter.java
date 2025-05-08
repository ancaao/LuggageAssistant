package com.example.luggageassistant.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PackingListEntry;

import java.util.List;

public class PackingGroupedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PackingListEntry> entries;

    public PackingGroupedAdapter(List<PackingListEntry> entries) {
        this.entries = entries;
    }

    @Override
    public int getItemViewType(int position) {
        return entries.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PackingListEntry.TYPE_CATEGORY) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PackingListEntry entry = entries.get(position);

        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).categoryTitle.setText(entry.getCategory());
        } else if (holder instanceof ItemViewHolder) {
            PackingItem item = entry.getItem();
            String label = item.getItem() + " (" + item.getQuantity() + ")";
            ((ItemViewHolder) holder).checkBox.setText(label);
            ((ItemViewHolder) holder).checkBox.setChecked(item.isChecked());
            ((ItemViewHolder) holder).checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> item.setChecked(isChecked));
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ItemViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.itemCheckBox);
        }
    }
}
