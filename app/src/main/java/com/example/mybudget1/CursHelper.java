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
    private static double XtoJuan = 55.0;
    private static double XtoEur = 420.0;
    private static double XtoJen = 3.0;
    private static double XtoLari = 150.0;




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

    public static double getToJuan() {return XtoJuan; }

    public static double getToEur() {return XtoEur;}

    public static double getToJen() {return XtoJen;}

    public static double getToLari() {return XtoLari;}


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
                                XtoJuan = rates.get("CNY");
                                XtoEur = rates.get("EUR");
                                XtoJen = rates.get("JPY");
                                XtoLari = rates.get("GEL");
                                break;

                            case "rubli":
                                XtoRubli = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoDollar = rates.get("USD");
                                XtoJuan = rates.get("CNY");
                                XtoEur = rates.get("EUR");
                                XtoJen = rates.get("JPY");
                                XtoLari = rates.get("GEL");
                                break;

                            case "dollar":
                                XtoDollar = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                XtoJuan = rates.get("CNY");
                                XtoEur = rates.get("EUR");
                                XtoJen = rates.get("JPY");
                                XtoLari = rates.get("GEL");
                                break;

                            case "CNY":
                                XtoJuan = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                XtoDollar = rates.get("USD");
                                XtoEur = rates.get("EUR");
                                XtoJen = rates.get("JPY");
                                XtoLari = rates.get("GEL");
                                break;

                            case "EUR":
                                XtoEur = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                XtoDollar = rates.get("USD");
                                XtoJuan = rates.get("CNY");
                                XtoJen = rates.get("JPY");
                                XtoLari = rates.get("GEL");
                                break;

                            case "JPY":
                                XtoJen = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                XtoDollar = rates.get("USD");
                                XtoJuan = rates.get("CNY");
                                XtoEur = rates.get("EUR");
                                XtoLari = rates.get("GEL");
                                break;

                            case "GEL":
                                XtoLari = 1.0;
                                XtoDram = rates.get("AMD");
                                XtoRubli = rates.get("RUB");
                                XtoDollar = rates.get("USD");
                                XtoJuan = rates.get("CNY");
                                XtoEur = rates.get("EUR");
                                XtoJen = rates.get("JPY");
                                break;

                            default:
                                listener.onError("Неизвестная базовая валюта: " + baseCurrencyCode);
                                return;
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
                            XtoJuan = getSavedRate(context, "dram", "CNY");
                            XtoEur = getSavedRate(context, "dram", "EUR");
                            XtoJen = getSavedRate(context, "dram", "JPY");
                            XtoLari = getSavedRate(context, "dram", "GEL");

                            if (XtoDollar == -1) XtoDollar = 0.0025;
                            if (XtoRubli == -1)  XtoRubli = 0.23;
                            if (XtoJuan == -1)    XtoJuan = 55.0;
                            if (XtoEur == -1)    XtoEur = 420.0;
                            if (XtoJen == -1)    XtoJen = 3.0;
                            if (XtoLari == -1)    XtoLari = 150.0;
                            break;

                        case "rubli":
                            XtoRubli = 1.0;
                            XtoDram = getSavedRate(context, "rubli", "AMD");
                            XtoDollar = getSavedRate(context, "rubli", "USD");
                            XtoJuan = getSavedRate(context, "rubli", "CNY");
                            XtoEur = getSavedRate(context, "rubli", "EUR");
                            XtoJen = getSavedRate(context, "rubli", "JPY");
                            XtoLari = getSavedRate(context, "rubli", "GEL");

                            if (XtoDram == -1)   XtoDram = 5.0;
                            if (XtoDollar == -1) XtoDollar = 0.011;
                            if (XtoJuan == -1)    XtoJuan = 8.0;
                            if (XtoEur == -1)    XtoEur = 90.0;
                            if (XtoJen == -1)    XtoJen = 1.5;
                            if (XtoLari == -1)    XtoLari = 25.0;
                            break;

                        case "dollar":
                            XtoDollar = 1.0;
                            XtoDram = getSavedRate(context, "dollar", "AMD");
                            XtoRubli = getSavedRate(context, "dollar", "RUB");
                            XtoJuan = getSavedRate(context, "dollar", "CNY");
                            XtoEur = getSavedRate(context, "dollar", "EUR");
                            XtoJen = getSavedRate(context, "dollar", "JPY");
                            XtoLari = getSavedRate(context, "dollar", "GEL");

                            if (XtoDram == -1)  XtoDram = 400.0;
                            if (XtoRubli == -1) XtoRubli = 90.0;
                            if (XtoJuan == -1)   XtoJuan = 7.0;
                            if (XtoEur == -1)   XtoEur = 0.9;
                            if (XtoJen == -1)   XtoJen = 150.0;
                            if (XtoLari == -1)   XtoLari = 2.6;
                            break;

                        case "CNY":
                            XtoJuan = 1.0;
                            XtoDram = getSavedRate(context, "CNY", "AMD");
                            XtoRubli = getSavedRate(context, "CNY", "RUB");
                            XtoDollar = getSavedRate(context, "CNY", "USD");
                            XtoEur = getSavedRate(context, "CNY", "EUR");
                            XtoJen = getSavedRate(context, "CNY", "JPY");
                            XtoLari = getSavedRate(context, "CNY", "GEL");

                            if (XtoDram == -1)   XtoDram = 55.0;
                            if (XtoRubli == -1)  XtoRubli = 8.0;
                            if (XtoDollar == -1) XtoDollar = 0.14;
                            if (XtoEur == -1)    XtoEur = 0.13;
                            if (XtoJen == -1)    XtoJen = 20.0;
                            if (XtoLari == -1)    XtoLari = 0.4;
                            break;

                        case "EUR":
                            XtoEur = 1.0;
                            XtoDram = getSavedRate(context, "EUR", "AMD");
                            XtoRubli = getSavedRate(context, "EUR", "RUB");
                            XtoDollar = getSavedRate(context, "EUR", "USD");
                            XtoJuan = getSavedRate(context, "EUR", "CNY");
                            XtoJen = getSavedRate(context, "EUR", "JPY");
                            XtoLari = getSavedRate(context, "EUR", "GEL");

                            if (XtoDram == -1)   XtoDram = 450.0;
                            if (XtoRubli == -1)  XtoRubli = 100.0;
                            if (XtoDollar == -1) XtoDollar = 1.1;
                            if (XtoJuan == -1)    XtoJuan = 8.0;
                            if (XtoJen == -1)    XtoJen = 160.0;
                            if (XtoLari == -1)    XtoLari = 3.0;
                            break;

                        case "JPY":
                            XtoJen = 1.0;
                            XtoDram = getSavedRate(context, "JPY", "AMD");
                            XtoRubli = getSavedRate(context, "JPY", "RUB");
                            XtoDollar = getSavedRate(context, "JPY", "USD");
                            XtoJuan = getSavedRate(context, "JPY", "CNY");
                            XtoEur = getSavedRate(context, "JPY", "EUR");
                            XtoLari = getSavedRate(context, "JPY", "GEL");

                            if (XtoDram == -1)   XtoDram = 3.0;
                            if (XtoRubli == -1)  XtoRubli = 0.07;
                            if (XtoDollar == -1) XtoDollar = 0.007;
                            if (XtoJuan == -1)    XtoJuan = 0.35;
                            if (XtoEur == -1)    XtoEur = 0.0065;
                            if (XtoLari == -1)    XtoLari = 0.018;
                            break;

                        case "GEL":
                            XtoLari = 1.0;
                            XtoDram = getSavedRate(context, "GEL", "AMD");
                            XtoRubli = getSavedRate(context, "GEL", "RUB");
                            XtoDollar = getSavedRate(context, "GEL", "USD");
                            XtoJuan = getSavedRate(context, "GEL", "CNY");
                            XtoEur = getSavedRate(context, "GEL", "EUR");
                            XtoJen = getSavedRate(context, "GEL", "JPY");

                            if (XtoDram == -1)   XtoDram = 150.0;
                            if (XtoRubli == -1)  XtoRubli = 25.0;
                            if (XtoDollar == -1) XtoDollar = 0.38;
                            if (XtoJuan == -1)    XtoJuan = 2.6;
                            if (XtoEur == -1)    XtoEur = 0.33;
                            if (XtoJen == -1)    XtoJen = 56.0;
                            break;

                        default:
                            listener.onError("Неизвестная базовая валюта: " + baseCurrencyCode);
                            return;
                    }

                    listener.onRatesUpdated();

                } catch (Exception e) {
                    listener.onError("Ошибка обработки сохранённых курсов: " + e.getMessage());
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
            case "CNY":
            case "juan":
                return "CNY";
            case "EUR":
            case "evro":
                return "EUR";
            case "JPY":
            case "jen":
                return "JPY";
            case "GEL":
            case "lari":
                return "GEL";

            default:
                return "USD";
        }
    }



    public static CursData getCursData(String currency) {
        switch (currency) {
            case "dollar":
            case "$":
                return new CursData("$", XtoDollar);

            case "rubli":
            case "₽":
                return new CursData("₽", XtoRubli);

            case "juan":
            case "yuan":
            case "元":
                return new CursData("元", XtoJuan);

            case "evro":
            case "eur":
            case "€":
                return new CursData("€", XtoEur);

            case "jen":
            case "jpy":
            case "¥":
                return new CursData("¥", XtoJen);

            case "lari":
            case "₾":
                return new CursData("₾", XtoLari);

            case "dram":
            case "֏":
                return new CursData("֏", XtoDram);

            default:
                throw new IllegalArgumentException("Неизвестная валюта: " + currency);
        }


    }
}
