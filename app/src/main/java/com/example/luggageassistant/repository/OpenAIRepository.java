package com.example.luggageassistant.repository;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.luggageassistant.model.ChatRequest;
import com.example.luggageassistant.model.ChatResponse;
import com.example.luggageassistant.model.Message;
import com.example.luggageassistant.repository.remote.OpenAIApi;
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
    private static final String API_KEY = "Bearer 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"; // 🛑 pune cheia reală aici

    private final OpenAIApi api;

    public OpenAIRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
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
        messages.add(new Message("user", "Te rog oferă o listă detaliată de obiecte pentru bagaj în funcție de aceste informații: \n" + promptJson));

        Log.d("GPT_PROMPT", "Trimitem promptul către GPT:\n" + promptJson);
    }
    public void generatePackingList(String promptJson, OnPackingListReceived callback) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", "Te rog oferă o listă detaliată de obiecte pentru bagaj în funcție de aceste informații: \n" + promptJson));

        Log.d("GPT_PROMPT", "Trimitem promptul către GPT:\n" + promptJson);

        ChatRequest request = new ChatRequest("gpt-4-turbo", messages, 0.7);

        api.getPackingList(request, API_KEY).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().choices.get(0).message.content;
                    saveResultToFirestore(promptJson, result);
                    callback.onSuccess(result);
                } else {
                    Log.e("OpenAI", "Eroare: " + response.code());
                    callback.onError("Eroare GPT: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e("OpenAI", "Eroare rețea: " + t.getMessage());
                callback.onError("Eroare rețea: " + t.getMessage());
            }
        });
    }

    private void saveResultToFirestore(String prompt, String result) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.put("prompt", prompt);
        data.put("response", result);
        data.put("timestamp", new Timestamp(new Date()));

        db.collection("ai_recommendations").add(data)
                .addOnSuccessListener(doc -> Log.d("Firestore", "Salvat cu succes"))
                .addOnFailureListener(e -> Log.e("Firestore", "Eroare salvare", e));
    }

    // Interfață callback simplă
    public interface OnPackingListReceived {
        void onSuccess(String response);
        void onError(String error);
    }
}