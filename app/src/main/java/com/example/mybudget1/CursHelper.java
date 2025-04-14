package com.example.mybudget1;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CursHelper {

    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/";
    private static double XtoDram = 390.0;
    private static double XtoRubli = 4.2;
    private static double XtoDollar = 4.2;



    public interface OnRatesUpdatedListener {
        void onRatesUpdated();
        void onError(String message);
    }

    public static double getToDram() {
        return XtoDram;
    }

    public static double getToDollar() {
        return XtoDollar;
    }

    public static double getToRub() {
        return XtoRubli;
    }

    public static void updateExchangeRates(Context context, OnRatesUpdatedListener listener) {
        DatabaseHelper2 dbHelper = new DatabaseHelper2(context);
        String baseCurrencyCode = dbHelper.getDefaultCurrency();
        String baseForApi = getApiCode(baseCurrencyCode);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExchangeRatesApi api = retrofit.create(ExchangeRatesApi.class);
        Call<ExchangeRatesResponse> call = api.getLatestRates(baseForApi);

        call.enqueue(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesResponse> call, Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Double> rates = response.body().getRates();

                    // ✅ Сохраняем локально
                    saveRatesLocally(context, baseCurrencyCode, rates);

                    try {
                        switch (baseCurrencyCode) {
                            case "dram":
                                XtoDram = 1.0;
                                XtoDollar = rates.get("USD");
                                XtoRubli = rates.get("RUB");
                                break;

                            case "rubli":
                                XtoRubli = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoDollar = rates.get("USD");
                                break;

                            case "dollar":
                                XtoDollar = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                break;
                        }

                        listener.onRatesUpdated();

                    } catch (Exception e) {
                        listener.onError("Ошибка обработки курсов: " + e.getMessage());
                    }

                } else {
                    listener.onError("Ошибка ответа: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ExchangeRatesResponse> call, Throwable t) {
                listener.onError("Сетевая ошибка: " + t.getMessage());

                try {
                    switch (baseCurrencyCode) {
                        case "dram":
                            XtoDram = 1.0;
                            XtoDollar = getSavedRate(context, "dram", "USD");
                            XtoRubli = getSavedRate(context, "dram", "RUB");

                            if (XtoDollar == -1) XtoDollar = 0.0025;
                            if (XtoRubli == -1)  XtoRubli = 0.23;
                            break;

                        case "rubli":
                            XtoRubli = 1.0;
                            XtoDram = getSavedRate(context, "rubli", "AMD");
                            XtoDollar = getSavedRate(context, "rubli", "USD");

                            if (XtoDram == -1)   XtoDram = 5.0;
                            if (XtoDollar == -1) XtoDollar = 0.011;
                            break;

                        case "dollar":
                            XtoDollar = 1.0;
                            XtoDram = getSavedRate(context, "dollar", "AMD");
                            XtoRubli = getSavedRate(context, "dollar", "RUB");

                            if (XtoDram == -1)  XtoDram = 400.0;
                            if (XtoRubli == -1) XtoRubli = 90.0;
                            break;
                    }

                    listener.onRatesUpdated();

                } catch (Exception e) {
                    listener.onError("Ошибка при использовании кэша: " + e.getMessage());
                }
            }
        });
    }


    private static void saveRatesLocally(Context context, String baseCurrency, Map<String, Double> rates) {
        SharedPreferences prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            editor.putFloat(baseCurrency + "_" + entry.getKey(), entry.getValue().floatValue());
        }

        editor.apply();
    }

    private static double getSavedRate(Context context, String baseCurrency, String targetCurrency) {
        SharedPreferences prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE);
        return prefs.getFloat(baseCurrency + "_" + targetCurrency, -1f);
    }



    private static String getApiCode(String code) {
        switch (code) {
            case "dram":
                return "AMD";
            case "rubli":
                return "RUB";
            case "dollar":
                return "USD";
            default:
                return "USD";
        }
    }



    public static CursData getCursData(String currency) {
        switch (currency) {
            case "$":
            case "dollar":
                return new CursData("$", XtoDollar);
            case "₽":
            case "rubli":
                return new CursData("₽", XtoRubli);
            case "֏":
            case "dram":
                return new CursData("֏", XtoDram);
            default:
                throw new IllegalArgumentException("Неизвестная валюта: " + currency);
        }
    }
}
