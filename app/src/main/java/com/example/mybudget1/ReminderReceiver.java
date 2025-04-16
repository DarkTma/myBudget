package com.example.mybudget1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "budget_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Напоминания о бюджете",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        String name = intent.getStringExtra("reminder_name");
        boolean isDefault = intent.getBooleanExtra("default_reminder", false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("Не забудь!");

        if (isDefault) {
            builder.setContentText("Запиши сегодняшние траты.");
        } else if (name != null) {
            builder.setContentText("Пришло время для траты: " + name);
        } else {
            builder.setContentText("Что-то важное, проверь приложение!");
        }

        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

}
