package com.example.mybudget1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupHelper {
    private Context context;
    private String[] dbNames = {"expenses.db", "finance.db", "budget.db"};

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



    public boolean restoreAllDatabases() {
        try {
            File backupDir = new File(context.getExternalFilesDir(null), "Backup");

            for (String dbName : dbNames) {
                File backupFile = new File(backupDir, dbName);
                if (!backupFile.exists()) return false;

                File dbFile = new File(context.getDatabasePath(dbName).getPath());

                try (FileInputStream fis = new FileInputStream(backupFile);
                     FileOutputStream fos = new FileOutputStream(dbFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

