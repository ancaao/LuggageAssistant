package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.repository.OpenAIRepository;

public class PackingListViewModel extends ViewModel {

    private final OpenAIRepository openAIRepository = new OpenAIRepository();
    private final MutableLiveData<String> packingListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<String> getPackingListLiveData() {
        return packingListLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void viewPromptforTest (String prompt) {
        openAIRepository.viewPrompt(prompt);
    }
    public void requestPackingList(String jsonPrompt, String tripId) {
        openAIRepository.generatePackingList(jsonPrompt, tripId, new OpenAIRepository.OnPackingListReceived() {
            @Override
            public void onSuccess(String response) {
                packingListLiveData.postValue(response);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }

}
