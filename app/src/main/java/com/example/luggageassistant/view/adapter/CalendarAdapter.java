package com.example.luggageassistant.view.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final LocalDate today;
    private LocalDate selectedDate;private final Map<LocalDate, List<TripConfiguration>> vacationDaysMap;


    @SuppressLint("NewApi")
    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, LocalDate selectedDate, Map<LocalDate, List<TripConfiguration>> vacationDaysMap)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;
        this.today = LocalDate.now();
        this.vacationDaysMap = vacationDaysMap;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String day = daysOfMonth.get(position);
        holder.dayOfMonth.setText(day);
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.background));
        holder.dayOfMonth.setTypeface(null, Typeface.NORMAL);

        if (!day.isEmpty()) {
            int dayInt = Integer.parseInt(day);
            LocalDate cellDate = selectedDate.withDayOfMonth(dayInt);

            boolean isToday = today.equals(cellDate);
            boolean isSelected = selectedDate.equals(cellDate);
            boolean hasTrip = vacationDaysMap.containsKey(cellDate);

            Context context = holder.itemView.getContext();

            if (isSelected && isToday && hasTrip) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error));
                holder.dayOfMonth.setTypeface(null, Typeface.BOLD);
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.onPrimary));

            } else if (isSelected) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.onPrimary));
                holder.dayOfMonth.setTypeface(null, Typeface.BOLD);
            } else if (isToday) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.tertiary));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.onPrimary));
            } else if (hasTrip) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.secondary));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.onPrimary));
            } else {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
            }
        }
    }


    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}