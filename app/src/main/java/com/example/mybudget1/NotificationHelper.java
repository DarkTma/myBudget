package com.example.mybudget1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "budget_reminder_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Budget Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Напоминания о ведении бюджета");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.my_primary));

        notificationManager.notify(1, builder.build());
    }
}
