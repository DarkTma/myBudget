package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReminderListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ReminderAdapter adapter;
    ImageButton btnBack;
    List<Reminder> reminderList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        btnBack = findViewById(R.id.buttonBackFromNotif);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ReminderListActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });

        recyclerView = findViewById(R.id.recyclerViewReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadReminders();
    }

    private void loadReminders() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getReminderList();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int kod = cursor.getInt(cursor.getColumnIndexOrThrow("requestCode"));
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            reminderList.add(new Reminder(id, timestamp, name, kod));
        }

        cursor.close();

        adapter = new ReminderAdapter(this , reminderList, new ReminderAdapter.OnReminderDeleteListener() {
            @Override
            public void onDelete(Reminder reminder) {
                // Отменяем Alarm
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Intent intent = new Intent(getApplicationContext(), ReminderReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            reminder.getRequestCode(), // теперь используем requestCode
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    alarmManager.cancel(pendingIntent); // Отменяем уведомление
                }

                // Удаляем из базы и обновляем список
                if (databaseHelper != null) {
                    databaseHelper.deleteReminder(reminder.getId()); // удаление по id
                }
                reminderList.remove(reminder);
                adapter.notifyDataSetChanged();
            }
        });




        recyclerView.setAdapter(adapter);
    }
}

