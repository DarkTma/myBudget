package com.example.mybudget1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private Context context;
    private List<Reminder> reminders;
    private OnReminderDeleteListener deleteListener;

    public interface OnReminderDeleteListener {
        void onDelete(Reminder reminder);
    }

    public ReminderAdapter(Context context , List<Reminder> reminders, OnReminderDeleteListener deleteListener) {
        this.context = context;
        this.reminders = reminders;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.nameText.setText(reminder.getName());

        // Форматируем дату и время из миллисекунд
        Date date = new Date(reminder.getTriggerAtMillis());
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.timeText.setText(format.format(date));

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView timeText;
        ImageButton deleteButton;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.reminderName);
            timeText = itemView.findViewById(R.id.reminderTime);
            deleteButton = itemView.findViewById(R.id.buttonDeleteNotif);
        }
    }
}



