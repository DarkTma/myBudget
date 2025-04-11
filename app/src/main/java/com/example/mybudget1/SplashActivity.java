package com.example.mybudget1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private DatabaseHelper2 databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper2(this);


        if (databaseHelper.getLastActivity().equals("")) {
            showCurrencySelectionDialog();
        } else {
            updateRatesAndGoToStart(this);
        }
    }

    private void updateRatesAndGoToStart(Context context) {
        CursHelper.updateExchangeRates(context, new CursHelper.OnRatesUpdatedListener() {
            @Override
            public void onRatesUpdated() {
                startActivity(new Intent(SplashActivity.this, StartActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Log.e("SplashActivity", "Ошибка обновления курсов: " + message);
                DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                databaseIncome.setCurs(databaseIncome.getDefaultCurrency());
                Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                intent.putExtra("error", "нет интернета , не меняйте курс валют");
                startActivity(intent);
                finish();
            }
        });
    }


    private void showCurrencySelectionDialog() {
        final String[] currencies = {"Драм", "Рубли", "Доллар"};
        final String[] currencyCodes = {"dram", "rubli", "dollar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите основную валюту")
                .setItems(currencies, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedCurrencyCode = currencyCodes[which];

                        databaseHelper.setDefaultCurrency(selectedCurrencyCode);
                        databaseHelper.setCurs(selectedCurrencyCode);
                        databaseHelper.setLastActivity();

                        updateRatesAndGoToStart(SplashActivity.this); // ← контекст нужен здесь
                    }
                })
                .setCancelable(false)
                .show();
    }


}

