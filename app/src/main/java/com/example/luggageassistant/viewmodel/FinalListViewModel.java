package com.example.luggageassistant.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.repository.OnItemsLoadedListener;
import com.example.luggageassistant.repository.PackingListRepository;

import java.util.List;

public class FinalListViewModel extends ViewModel {

    private final MutableLiveData<List<PackingItem>> itemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final PackingListRepository repository = PackingListRepository.getInstance();

    public LiveData<List<PackingItem>> getItems() {
        return itemsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadItems(String userId, String tripId) {
        repository.getFinalPackingItems(userId, tripId, new OnItemsLoadedListener() {
            @Override
            public void onItemsLoaded(List<PackingItem> items) {
                itemsLiveData.setValue(items);
            }
            @Override
            public void onError(Exception e) {
                errorLiveData.postValue("Error loading items: " + e.getMessage());
            }
        });
    }
}
