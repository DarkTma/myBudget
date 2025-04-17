package com.example.mybudget1;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Notes> noteList;

    public NoteAdapter(List<Notes> noteList) {
        this.noteList = noteList;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteName, noteType, noteDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteName = itemView.findViewById(R.id.noteName);
            noteType = itemView.findViewById(R.id.noteType);
            noteDate = itemView.findViewById(R.id.noteDate);
        }
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Notes note = noteList.get(position);

        holder.noteName.setText(note.getName());
        int colorName = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
        holder.noteName.setTextColor(colorName);

        if(note.getType().equals("Income")) {
            holder.noteType.setText("Доход");
        } else if (note.getType().equals("Spent")) {
            holder.noteType.setText("Расход");
        } else{
            holder.noteType.setText("Заметка");
        }

        // Цвет в зависимости от типа
        int color;
        if(note.getType().equals("Income")) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_green);
        } else if (note.getType().equals("Spent")) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_red);
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_yellow);
            holder.noteType.setTextColor(Color.BLACK);
        }

        holder.noteType.setBackgroundColor(color);

        int colordata;
        if (note.getAction().equals("add")) {
            colordata = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_green);
        } else if (note.getAction().equals("delete")) {
            colordata = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_red);
        } else if(note.getAction().equals("edit")){
            colordata = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_orange);
        } else {
            colordata = ContextCompat.getColor(holder.itemView.getContext(), R.color.my_dark_yellow);
            holder.noteDate.setTextColor(Color.BLACK);
        }

        holder.noteDate.setBackgroundColor(colordata);

        holder.noteDate.setText(note.getTriggerAtMillis());
    }


    @Override
    public int getItemCount() {
        return noteList.size();
    }
}

