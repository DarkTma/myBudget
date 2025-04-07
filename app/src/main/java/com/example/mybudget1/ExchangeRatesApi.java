package com.example.mybudget1;

import com.example.mybudget1.ExchangeRatesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ExchangeRatesApi {
    @GET("latest/{base}")
    Call<ExchangeRatesResponse> getLatestRates(@Path("base") String baseCurrency);
}
