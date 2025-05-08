package com.example.luggageassistant.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;

import java.util.List;

public class PackingItemAdapter extends RecyclerView.Adapter<PackingItemAdapter.PackingItemViewHolder> {

    private List<PackingItem> items;

    public PackingItemAdapter(List<PackingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PackingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_packing_checkbox, parent, false);
        return new PackingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackingItemViewHolder holder, int position) {
        PackingItem item = items.get(position);
        String label = item.getItem() + " (" + item.getQuantity() + ")";
        holder.checkBox.setText(label);
        holder.checkBox.setChecked(item.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> item.setChecked(isChecked));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PackingItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        PackingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.itemCheckBox);
        }
    }
}
