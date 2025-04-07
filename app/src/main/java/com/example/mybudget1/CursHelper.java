package com.example.mybudget1;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CursHelper {

    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/";
    private static double dramToDollar = 390.0;
    private static double dramToRub = 4.2;

    public interface OnRatesUpdatedListener {
        void onRatesUpdated();
        void onError(String message);
    }

    public static void updateExchangeRates(OnRatesUpdatedListener listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExchangeRatesApi api = retrofit.create(ExchangeRatesApi.class);
        Call<ExchangeRatesResponse> call = api.getLatestRates("USD"); // USD как базовая

        call.enqueue(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesResponse> call, Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeRatesResponse rates = response.body();
                    if (rates.getRates().containsKey("AMD")) {
                        dramToDollar = rates.getRates().get("AMD");
                    }
                    if (rates.getRates().containsKey("RUB")) {
                        double rubToDram = rates.getRates().get("AMD") / rates.getRates().get("RUB");
                        dramToRub = rubToDram;
                    }
                    listener.onRatesUpdated();
                } else {
                    listener.onError("Ошибка ответа: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRatesResponse> call, Throwable t) {
                listener.onError("Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    public static CursData getCursData(String currency) {
        switch (currency) {
            case "$":
            case "dollar":
                return new CursData("$", 1.0 / dramToDollar);
            case "₽":
            case "rubli":
                return new CursData("₽", 1.0 / dramToRub);
            case "֏":
            case "dram":
                return new CursData("֏", 1.0);
            default:
                throw new IllegalArgumentException("Неизвестная валюта: " + currency);
        }
    }

    public static double getDramToDollar() {
        return dramToDollar;
    }

    public static double getDramToRub() {
        return dramToRub;
    }
}
