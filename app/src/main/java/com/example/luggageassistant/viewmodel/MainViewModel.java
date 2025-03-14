package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.repository.MainRepository;

public class MainViewModel extends ViewModel {

    private final MainRepository mainRepository;
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutStatus = new MutableLiveData<>();

    public MainViewModel() {
        mainRepository = new MainRepository();
    }

    public void checkIfUserIsLoggedIn() {
        String email = mainRepository.getUserEmail();
        userEmail.postValue(email);
    }

    public void logoutUser() {
        mainRepository.logout();
        logoutStatus.postValue(true);
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<Boolean> getLogoutStatus() {
        return logoutStatus;
    }
}
