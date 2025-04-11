package com.example.mybudget1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private DatabaseHelper2 databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoImage);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        logo.startAnimation(pulse);

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
                new android.os.Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, StartActivity.class));
                    finish();
                }, 1);
            }

            @Override
            public void onError(String message) {
                new android.os.Handler().postDelayed(() -> {
                Log.e("SplashActivity", "Ошибка обновления курсов: " + message);
                DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                databaseIncome.setCurs(databaseIncome.getDefaultCurrency());
                Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                intent.putExtra("error", "нет интернета , не меняйте курс валют");
                startActivity(intent);
                finish();
                }, 1);
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

