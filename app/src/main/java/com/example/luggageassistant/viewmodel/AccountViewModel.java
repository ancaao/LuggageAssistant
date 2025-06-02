package com.example.luggageassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.luggageassistant.model.User;
import com.example.luggageassistant.repository.AccountRepository;
import com.example.luggageassistant.view.AccountFragment.Callback;

public class AccountViewModel extends ViewModel {

    private final AccountRepository accountRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutStatus = new MutableLiveData<>();

    public AccountViewModel() {
        accountRepository = new AccountRepository();
    }

    public void loadUserData() {
        accountRepository.getUserData(user -> userLiveData.postValue(user));
    }

    public void updateField(String field, String value) {
        accountRepository.updateUserField(field, value);
    }

    public void logoutUser() {
        accountRepository.logout();
        logoutStatus.postValue(true);
    }

    public void deleteUser(String password, Callback callback) {
        accountRepository.deleteAccount(password, callback);
    }
    public void sendResetPasswordEmail(String email, Callback callback) {
        accountRepository.sendPasswordResetEmail(email, callback);
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getLogoutStatus() {
        return logoutStatus;
    }
}
