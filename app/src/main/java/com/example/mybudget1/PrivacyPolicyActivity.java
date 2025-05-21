package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;


import com.google.api.services.drive.model.FileList;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicyActivity";
    private static final String BACKUP_FILENAME = "mybudget_backup.zip";

    private static final String PRIVACY_POLICY_URL = "https://www.dropbox.com/scl/fi/77noykv1p6a28ne5csxn6/.pdf?rlkey=dmhtmg8bb4y3s5ekxws243m2z&st=83tke5wq&dl=0";

    private ImageButton buttonBackFromConf;
    private GoogleSignInClient googleSignInClient;
    private Drive driveService;
    private DriveHelper driveHelper;
    private BackupHelper backupHelper;
    private ActivityResultLauncher<Intent> authPermissionLauncher;
    private GoogleSignInAccount googleSignInAccount;
    private ActivityResultLauncher<Intent> signInLauncher;


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

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            driveHelper = new DriveHelper(this, account, authPermissionLauncher);
            backupHelper = new BackupHelper(this);
        }

        TextView link = findViewById(R.id.privacy_link);
        link.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL));
            startActivity(browserIntent);
        });

        Button signOutButton = findViewById(R.id.signout);

        SharedPreferences pref = getSharedPreferences("backup_prefs", MODE_PRIVATE);
        String accountEmail = pref.getString("GOOGLE_ACCOUNT_EMAIL", null);

        if (accountEmail != null) {
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            signOutButton.setVisibility(View.GONE);
        }

        signOutButton.setOnClickListener(v -> signOutFromGoogleAccount());

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Инициализируем signInLauncher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            googleSignInAccount = task.getResult(ApiException.class);
                            if (googleSignInAccount != null) {
                                SharedPreferences prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);
                                prefs.edit().putString("GOOGLE_ACCOUNT_EMAIL", googleSignInAccount.getEmail()).apply();

                                setupDriveService(googleSignInAccount);  // Инициализируем driveService
                                checkAndPromptBackupOption();             // Проверяем наличие бэкапа и показываем диалог
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        authPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (googleSignInAccount != null) {
                            DriveHelper driveHelper = new DriveHelper(this, googleSignInAccount, authPermissionLauncher);
                            driveHelper.uploadBackupFile(new File(getFilesDir(), "backup.zip"));
                        }
                    } else {
                        Toast.makeText(this, "Разрешение не получено", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        Button buttonBackup = findViewById(R.id.backup);
        buttonBackup.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                signInLauncher.launch(googleSignInClient.getSignInIntent());
            });
//            if (googleSignInAccount == null) {
//                signInLauncher.launch(googleSignInClient.getSignInIntent());
//            } else {
//                setupDriveService(googleSignInAccount);
//                checkAndPromptBackupOption();
//            }
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


        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isAuthEnabled = preferences.getBoolean("auth_enabled", false);
        authSwitch.setChecked(isAuthEnabled);

        authSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("auth_enabled", isChecked);
            editor.apply();
        });
    }

    private void setupDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singletonList("https://www.googleapis.com/auth/drive.file"));
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("MyBudget").build();
    }

    private void checkAndPromptBackupOption() {
        driveHelper = new DriveHelper(this,googleSignInAccount,authPermissionLauncher);
        backupHelper = new BackupHelper(this);
        driveHelper.checkIfBackupExists(fileExists -> {
            if (fileExists) {
                new AlertDialog.Builder(this)
                        .setTitle("Резервная копия найдена")
                        .setMessage("На Google Диске найдена резервная копия. Что вы хотите сделать?")
                        .setPositiveButton("Загрузить", (dialog, which) -> {
                            java.io.File targetFile = new java.io.File(getFilesDir(), "mybudget_backup.zip");

                            // Запускаем в фоне, чтобы не блокировать UI
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());

                            executor.execute(() -> {
                                driveHelper.downloadBackupFile(targetFile);

                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    Log.e("RestoreProcess", "Ожидание прервано.", e);
                                }

                                boolean success = backupHelper.restoreBackupFromZip(targetFile);

                                handler.post(() -> {
                                    if (success) {
                                        Toast.makeText(this, "Резервная копия успешно восстановлена", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(() -> {
                                            finishAffinity();
                                            System.exit(0);
                                        }, 1500);
                                    } else {
                                        Toast.makeText(this, "Ошибка восстановления резервной копии (возможно, файл не скачался)", Toast.LENGTH_LONG).show();
                                    }
                                });
                                executor.shutdown(); // Завершаем executor
                            });
                        })

                        .setNegativeButton("Перезаписать/обновить", (dialog, which) -> {
                            DatabaseHelper databaseHelper = new DatabaseHelper(this);
                            DatabaseHelper2 databaseHelper2 = new DatabaseHelper2(this);
                            if (databaseHelper != null) {
                                databaseHelper.close();
                                Log.d("BackupProcess", "databaseHelper закрыт.");
                            }
                            if (databaseHelper2 != null) {
                                databaseHelper2.close();
                                Log.d("BackupProcess", "databaseHelper2 закрыт.");
                            }


                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());

                            executor.execute(() -> {
                                java.io.File zipFile = null;
                                try {
                                    zipFile = backupHelper.createBackupZipFile();
                                    if (zipFile != null) {
                                        driveHelper.uploadBackupFile(zipFile);
                                    } else {
                                        handler.post(() -> Toast.makeText(this, "Ошибка: Не удалось создать файл резервной копии.", Toast.LENGTH_LONG).show());
                                    }
                                } catch (Exception e) {
                                    Log.e("BackupProcess", "Произошла ошибка в процессе бэкапа: " + e.getMessage(), e);
                                    handler.post(() -> Toast.makeText(this, "Непредвиденная ошибка в процессе бэкапа.", Toast.LENGTH_LONG).show());
                                } finally {

                                    handler.post(() -> {

                                        DatabaseHelper databaseHelperr;
                                        DatabaseHelper2 databaseHelper22;
                                         databaseHelperr = new DatabaseHelper(this);
                                         databaseHelperr.getWritableDatabase();
                                         databaseHelper22 = new DatabaseHelper2(this);
                                         databaseHelper22.getWritableDatabase();
                                    });
                                    executor.shutdown(); // Важно завершить executor
                                }
                            });
                        })
                        .setNeutralButton("Отмена", null)
                        .show();
            } else {
                java.io.File zipFile = backupHelper.createBackupZipFile();
                if (zipFile != null) {
                    driveHelper.uploadBackupFile(zipFile);
                }
            }
        });
    }


    private void signOutFromGoogleAccount() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Очистить сохранённые данные
            SharedPreferences prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);
            prefs.edit().remove("GOOGLE_ACCOUNT_EMAIL").apply();

            // Также можно обнулить локальную переменную
            googleSignInAccount = null;

            Toast.makeText(this, "Аккаунт успешно отвязан", Toast.LENGTH_SHORT).show();
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

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
                if (result != null) {
                    importDatabasesFromZip(result);
                }
            });

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
