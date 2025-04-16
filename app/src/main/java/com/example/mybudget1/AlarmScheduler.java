package com.example.mybudget1;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;


import java.util.Calendar;

public class AlarmScheduler {

    public static void scheduleReminder(Context context, long triggerAtMillis, String name, int requestCode) {
        // Создание канала уведомлений для Android 8.0 и выше
        String channelId = "budget_channel_id";
        String channelName = "Напоминания о бюджете";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Напоминания записывать траты");
            notificationManager.createNotificationChannel(channel);
        }

        // Планируем уведомление
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_name", name); // Передаем имя через Intent

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    public static void scheduleDailyReminder(Context context) {
        String channelId = "budget_channel_id";
        String channelName = "Напоминания о бюджете";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Напоминания записывать траты");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("default_reminder", true); // чтобы отличать тип в onReceive

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1000, // уникальный код, чтобы не пересекался с пользовательскими
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

}
