package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import com.google.api.services.drive.model.FileList;




import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriveHelper {
    private final Drive driveService;
    private final Context context;
    private final ActivityResultLauncher<Intent> authPermissionLauncher;
    private static final String BACKUP_FILENAME = "mybudget_backup.zip";

    public DriveHelper(Context context, GoogleSignInAccount account, ActivityResultLauncher<Intent> authPermissionLauncher) {
        this.context = context;
        this.authPermissionLauncher = authPermissionLauncher;
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList("https://www.googleapis.com/auth/drive.file"));
        credential.setSelectedAccount(account.getAccount());

        this.driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("MyBudget").build();
    }

    public void uploadBackupFile(java.io.File localFile) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                FileList existingFiles = driveService.files().list()
                        .setQ("name = '" + BACKUP_FILENAME + "' and trashed = false")
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute();

                if (existingFiles.getFiles() != null && !existingFiles.getFiles().isEmpty()) {
                    for (File file : existingFiles.getFiles()) {
                        driveService.files().delete(file.getId()).execute();
                    }
                }

                File fileMetadata = new File();
                fileMetadata.setName(BACKUP_FILENAME);

                com.google.api.client.http.FileContent mediaContent =
                        new com.google.api.client.http.FileContent("application/zip", localFile);

                File uploadedFile = driveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id, name")
                        .execute();

                handler.post(() -> Toast.makeText(context, "Резервная копия загружена в Google Диск", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();

                if (e instanceof UserRecoverableAuthIOException) {
                    Intent intent = ((UserRecoverableAuthIOException) e).getIntent();
                    if (context instanceof Activity) {
                        handler.post(() -> authPermissionLauncher.launch(intent));
                    }
                }

                String errorMessage = (e.getMessage() != null) ? e.getMessage() : "Неизвестная ошибка";
                handler.post(() -> {
                    Toast.makeText(context, "Ошибка при загрузке на Google Диск", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(context, "Неизвестная ошибка при загрузке на Google Диск", Toast.LENGTH_SHORT).show();
                });
            } finally {
                executor.shutdown();
            }
        });
    }






    public Drive getDriveService() {
        return driveService;
    }

    public void downloadBackupFile(java.io.File targetFile) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean success = false;
            try {
                FileList result = driveService.files().list()
                        .setQ("name = '" + BACKUP_FILENAME + "' and trashed = false")
                        .setFields("files(id, name)")
                        .execute();

                if (!result.getFiles().isEmpty()) {
                    String fileId = result.getFiles().get(0).getId();
                    driveService.files().get(fileId)
                            .executeMediaAndDownloadTo(new java.io.FileOutputStream(targetFile));
                    success = true;
                }
            } catch (IOException e) {
                Log.e("DriveDownloadError", "Error downloading file", e);
            }

            boolean finalSuccess = success;
            handler.post(() -> {
                if (finalSuccess) {

                } else {
                    Toast.makeText(context, "Файл не найден в Google Диске", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    public interface BackupCheckCallback {
        void onResult(boolean fileExists);
    }

    public void checkIfBackupExists(BackupCheckCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean exists = false;
            try {
                FileList result = driveService.files().list()
                        .setQ("name = '" + BACKUP_FILENAME + "' and trashed = false")
                        .setFields("files(id)")
                        .execute();
                exists = !result.getFiles().isEmpty();
            } catch (UserRecoverableAuthIOException e) {
                Intent intent = e.getIntent();
                if (context instanceof Activity && authPermissionLauncher != null) {
                    handler.post(() -> authPermissionLauncher.launch(intent));
                }
                return;
            } catch (Exception e) {
                Log.e("DriveHelper", "Ошибка при проверке резервной копии", e);
            }

            boolean finalExists = exists;
            handler.post(() -> callback.onResult(finalExists));
        });
    }
}


