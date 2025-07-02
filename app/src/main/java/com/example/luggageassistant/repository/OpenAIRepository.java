package com.example.luggageassistant.repository;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.luggageassistant.model.ChatRequest;
import com.example.luggageassistant.model.ChatResponse;
import com.example.luggageassistant.model.Message;
import com.example.luggageassistant.repository.remote.OpenAIApi;
import static com.example.luggageassistant.constants.Constants.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenAIRepository {

    private static final String BASE_URL = "https://api.openai.com/";
    private static final String API_KEY = OPENAI_API_KEY;

    private final OpenAIApi api;
    private String tripId;

    public OpenAIRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        api = retrofit.create(OpenAIApi.class);
    }

    public void viewPrompt (String promptJson) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", promptJson));
    }
    public void generatePackingList(String promptJson, String tripId, OnPackingListReceived callback) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", promptJson));

        ChatRequest request = new ChatRequest("gpt-4-turbo", messages, 0.7);

        api.getPackingList(request, API_KEY).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().choices.get(0).message.content;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    saveResultToFirestore(promptJson, result, userId, tripId);
                    callback.onSuccess(result);
                } else {
                    Log.e("OpenAI", "Error: " + response.code());
                    callback.onError("GPT error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e("OpenAI", "Network error: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void saveResultToFirestore(String prompt, String result, String userId, String tripId) {
        Map<String, Object> data = new HashMap<>();
        data.put("prompt", prompt);
        data.put("response", result);
        data.put("timestamp", new Timestamp(new Date()));

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("ai_recommendations")
                .add(data)
                .addOnSuccessListener(doc -> Log.d("Firestore", "Saved recommendation"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving", e));
    }

    public interface OnPackingListReceived {
        void onSuccess(String response);
        void onError(String error);
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}