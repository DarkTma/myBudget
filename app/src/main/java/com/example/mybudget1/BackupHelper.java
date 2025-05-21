package com.example.mybudget1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupHelper {
    private Context context;
    private String[] dbNames = {"expenses.db", "finance.db", "budget.db"};

    private static final String BACKUP_FILENAME = "mybudget_backup.zip";
    public BackupHelper(Context context) {
        this.context = context;
    }


    public void backupAndShareDatabases(Activity activity) {
        try {
            File backupDir = new File(context.getExternalFilesDir(null), "Backup");
            if (!backupDir.exists()) backupDir.mkdirs();

            File zipFile = new File(backupDir, "mybudget_backup.zip");

            // Создание zip архива
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (String dbName : dbNames) {
                    File dbFile = context.getDatabasePath(dbName);
                    if (!dbFile.exists()) continue;

                    try (FileInputStream fis = new FileInputStream(dbFile)) {
                        ZipEntry entry = new ZipEntry(dbName);
                        zos.putNextEntry(entry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }

                        zos.closeEntry();
                    }
                }
            }

            Toast.makeText(context, "Резервная копия создана!", Toast.LENGTH_SHORT).show();

            // ➤ Поделиться zip-файлом
            shareBackupZipFile(activity, zipFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при создании архива", Toast.LENGTH_SHORT).show();
        }
    }





    public File createBackupZipFile() {
        try {
            File backupDir = new File(context.getExternalFilesDir(null), "Backup");
            if (!backupDir.exists()) backupDir.mkdirs();

            File zipFile = new File(backupDir, "mybudget_backup.zip");


            // Создание zip архива
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                boolean anyDbFound = false; // Флаг для отслеживания, были ли добавлены какие-либо БД
                for (String dbName : dbNames) {
                    File dbFile = context.getDatabasePath(dbName);
                    Log.d("BackupDebug", "Проверяю файл базы данных: " + dbName +
                            ", Полный путь: " + dbFile.getAbsolutePath() +
                            ", Существует: " + dbFile.exists()); // <-- ДОБАВЬТЕ ЭТОТ ЛОГ!

                    if (!dbFile.exists()) {
                        continue;
                    }

                    // Если файл найден, устанавливаем флаг
                    anyDbFound = true;

                    try (FileInputStream fis = new FileInputStream(dbFile)) {
                        ZipEntry entry = new ZipEntry(dbName);
                        zos.putNextEntry(entry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }

                        zos.closeEntry();
                    }
                }
            }


            return zipFile;

        } catch (Exception e) {
            Log.e("BackupDebug" , "Ошибка при создании ZIP-файла: " + e.getMessage(), e); // Логируйте весь стек трейс
            return null;
        }
    }

    public boolean restoreBackupFromZip(File zipFile) {
        if (!zipFile.exists() || zipFile.length() == 0) {
            return false;
        }

        File tempDir = new File(context.getCacheDir(), "backup_restore_temp");
        if (tempDir.exists()) {
            deleteRecursive(tempDir);
        }
        tempDir.mkdirs();

        try {
            unzipBackupFile(zipFile, tempDir);

            for (String dbName : dbNames) {
                File newDbFile = new File(tempDir, dbName);
                if (!newDbFile.exists()) continue;

                File currentDbFile = context.getDatabasePath(dbName);
                copyFile(newDbFile, currentDbFile);
            }

            deleteRecursive(tempDir);
            return true;

        } catch (IOException e) {
            deleteRecursive(tempDir);
            return false;
        } catch (Exception e) {
            deleteRecursive(tempDir);
            return false;
        }
    }


    private void unzipBackupFile(File zipFile, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDir, entry.getName());
                new File(newFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }





    private void shareBackupZipFile(Activity activity, File zipFile) {
        if (!zipFile.exists()) {
            Toast.makeText(context, "Файл архива не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", zipFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.startActivity(Intent.createChooser(intent, "Поделиться резервной копией"));
    }
}
