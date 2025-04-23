package com.example.mybudget1;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeminiService {
    @POST("v1beta/models/gemini-1.5-pro:generateContent")
    Call<GeminiResponse> generateResponse(
            @Query("key") String apiKey,
            @Body RequestBody body
    );
}



