package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.repository.LoginRepository;

public class LoginViewModel extends ViewModel {
    private final LoginRepository loginRepository;
    private final MutableLiveData<Boolean> isLoginSuccessful = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel() {
        loginRepository = new LoginRepository();
    }

    public LiveData<Boolean> getIsLoginSuccessful() {
        return isLoginSuccessful;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Email or password cannot be empty");
            return;
        }

        isLoading.setValue(true);
        loginRepository.login(email, password, (success, error) -> {
            isLoading.postValue(false);
            if (success) {
                isLoginSuccessful.postValue(true);
            } else {
                errorMessage.postValue(error);
            }
        });
    }
}
