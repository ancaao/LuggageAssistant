package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.repository.RegisterRepository;
import com.example.luggageassistant.repository.EmailCheckCallback;


public class RegisterViewModel extends ViewModel {

    private final RegisterRepository registerRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>();

    public RegisterViewModel() {
        registerRepository = new RegisterRepository();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }

    public void checkIfEmailExists(String email, EmailCheckCallback callback) {
        registerRepository.checkIfEmailExists(email, callback);
    }

    public void registerUser(String email, String password, String firstName, String lastName, String phone, RegisterRepository.RegistrationCallback callback) {
        isLoading.setValue(true);

        registerRepository.checkIfEmailExists(email, exists -> {
            if (exists) {
                isLoading.postValue(false);
                callback.onComplete(false); // Email deja folosit
            } else {
                registerRepository.registerUser(email, password, firstName, lastName, phone, success -> {
                    isLoading.postValue(false);
                    callback.onComplete(success);
                });
            }
        });
    }


}
