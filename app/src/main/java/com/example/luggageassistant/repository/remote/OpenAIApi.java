package com.example.luggageassistant.repository.remote;

import com.example.luggageassistant.model.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Header;
import com.example.luggageassistant.model.ChatRequest;

public interface OpenAIApi {
    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1/chat/completions")
    Call<ChatResponse> getPackingList(
            @Body ChatRequest request,
            @Header("Authorization") String authHeader
    );
}
