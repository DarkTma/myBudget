package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ImageButton buttonBackFromConf;
    private static final String PRIVACY_POLICY_URL = "https://www.dropbox.com/scl/fi/77noykv1p6a28ne5csxn6/.pdf?rlkey=dmhtmg8bb4y3s5ekxws243m2z&st=83tke5wq&dl=0";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        buttonBackFromConf = findViewById(R.id.buttonBackFromConf);
        buttonBackFromConf.setOnClickListener(v -> {
            Intent intent = new Intent(PrivacyPolicyActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });

        TextView link = findViewById(R.id.privacy_link);
        link.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL));
            startActivity(browserIntent);
        });

        Button zbros = findViewById(R.id.zbros);
        zbros.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Сбросить данные?")
                    .setMessage("Все данные будут удалены. Вы уверены?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        boolean e = deleteDatabase("expenses.db");
                        boolean f = deleteDatabase("finance.db");
                        boolean b = deleteDatabase("budget.db");
                        Toast.makeText(this, (e && f && b) ? "Сброшено!" : "Ошибка!", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        Switch switchQuickEntry = findViewById(R.id.switchQuickEntry);
        boolean isRunning = isQuickEntryNotificationActive();
        switchQuickEntry.setChecked(isRunning);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean("quick_entry_enabled", isRunning).apply();

        switchQuickEntry.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("quick_entry_enabled", isChecked);
            editor.apply();

            if (isChecked) {
                Intent serviceIntent = new Intent(this, QuickExpenseService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(serviceIntent);
                } else {
                    this.startService(serviceIntent);
                }
                Toast.makeText(this, "Быстрая запись включена", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, QuickExpenseService.class));
                Toast.makeText(this, "Быстрая запись отключена", Toast.LENGTH_SHORT).show();
            }
        });

        Switch authSwitch = findViewById(R.id.switch_auth);
        Button buttonBackup = findViewById(R.id.backup);
        Button buttonRestore = findViewById(R.id.restore);

        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isAuthEnabled = preferences.getBoolean("auth_enabled", false);
        authSwitch.setChecked(isAuthEnabled);

        authSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("auth_enabled", isChecked);
            editor.apply();
        });

        BackupHelper backupHelper = new BackupHelper(this);

        buttonBackup.setOnClickListener(v -> {
            backupHelper.backupAndShareDatabases(this);
        });

        // Запуск восстановления данных
        buttonRestore.setOnClickListener(v -> {
            openDocumentLauncher.launch(new String[]{"application/zip"}); // Только для zip-файлов
        });
    }

    private boolean isQuickEntryNotificationActive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            for (android.service.notification.StatusBarNotification sbn : notificationManager.getActiveNotifications()) {
                if (sbn.getId() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // Этот метод запускает выбор zip-файла для восстановления
    // Этот метод запускает выбор zip-файла для восстановления
    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
                if (result != null) {
                    importDatabasesFromZip(result);
                }
            });



    // Метод для восстановления баз данных из zip-файла
    private void importDatabasesFromZip(Uri zipUri) {
        try (InputStream is = getContentResolver().openInputStream(zipUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String dbName = entry.getName();

                // Убедимся, что мы восстанавливаем только нужные базы
                if (!Arrays.asList("expenses.db", "finance.db", "budget.db").contains(dbName)) continue;

                File outFile = getDatabasePath(dbName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                zis.closeEntry();
            }

            Toast.makeText(this, "Базы успешно восстановлены!", Toast.LENGTH_SHORT).show();
            finishAffinity(); // Закрывает все Activity в стеке
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при восстановлении данных", Toast.LENGTH_SHORT).show();
        }
    }
}
