package com.example.luggageassistant.repository;

import com.example.luggageassistant.model.PackingItem;

import java.util.List;


public interface OnItemsLoadedListener {
    void onItemsLoaded(List<PackingItem> items);
    void onError(Exception e);
}