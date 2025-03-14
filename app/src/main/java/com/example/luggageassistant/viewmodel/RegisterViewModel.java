package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.repository.RegisterRepository;

public class RegisterViewModel extends ViewModel {

    private final RegisterRepository registerRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>();

    public RegisterViewModel() {
        registerRepository = new RegisterRepository();
    }

    public void registerUser(String email, String password, String firstName, String lastName, String phone) {
        isLoading.setValue(true);
        registerRepository.registerUser(email, password, firstName, lastName, phone, success -> {
            isLoading.postValue(false);
            registrationStatus.postValue(success);
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }
}
