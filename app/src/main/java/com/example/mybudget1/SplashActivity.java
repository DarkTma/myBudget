package com.example.mybudget1;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class SplashActivity extends AppCompatActivity {

    private DatabaseHelper2 databaseHelper;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoImage);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        logo.startAnimation(pulse);

        databaseHelper = new DatabaseHelper2(this);

        // Проверяем разрешение и создаём канал/ставим напоминание
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Если разрешение не получено, запросим его
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            } else {
                // Разрешение уже предоставлено — создаём канал уведомлений и выполняем основную логику
                createNotificationChannel();
                AlarmScheduler.scheduleDailyReminder(this);
                continueAfterPermissionGranted();
            }
        } else {
            // До Android 13 — не нужно разрешение
            createNotificationChannel();
            AlarmScheduler.scheduleDailyReminder(this);
            continueAfterPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено — создаём канал уведомлений и ставим напоминания
                createNotificationChannel();
                AlarmScheduler.scheduleDailyReminder(this);
            } else {
                // Разрешение не получено, можно отобразить предупреждение
                Toast.makeText(this, "Разрешение на уведомления не предоставлено.", Toast.LENGTH_SHORT).show();
            }

            continueAfterPermissionGranted();
        }
    }

    private void continueAfterPermissionGranted() {
        if (databaseHelper.getLastActivity().equals("")) {
            showCurrencySelectionDialog();
        } else {
            updateRatesAndGoToStart(this);
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "budget_channel_id";
            String channelName = "Напоминания о бюджете";
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel existingChannel = notificationManager.getNotificationChannel(channelId);
            if (existingChannel == null) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Канал для важных напоминаний о бюджете");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 250, 500});
                notificationManager.createNotificationChannel(channel);
            }
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
                intent.putExtra("error", "нет интернета , невозможно обновить курс валют");
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

