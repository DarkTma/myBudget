package com.example.mybudget1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Загружаем курсы валют
        CursHelper.updateExchangeRates(new CursHelper.OnRatesUpdatedListener() {
            @Override
            public void onRatesUpdated() {
                // После загрузки переходим на главный экран
                startActivity(new Intent(SplashActivity.this, StartActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Log.e("SplashActivity", "Ошибка обновления курсов : " + message);
                // Можно показать ошибку и всё равно перейти в MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
