package com.example.mybudget1;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NoteAdapter adapter;
    private EditText searchEditText;
    private Spinner filterSpinner;
    private List<Notes> allNotes = new ArrayList<>();
    private List<Notes> filteredNotes = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_activity);

        refreshList();

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        filterSpinner = findViewById(R.id.filterSpinner);
        Button addNoteButton = findViewById(R.id.buttonAddNote);
        ImageButton backButton = findViewById(R.id.buttonBackFromNotes);

        adapter = new NoteAdapter(filteredNotes);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(adapter);

        setupSearch();
        setupSpinner();

        addNoteButton.setOnClickListener(v -> {
            // Создание контейнера
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 40, 40, 10);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_background));

            // Поле ввода
            EditText editText = new EditText(this);
            editText.setHint("Введите заметку...");
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setLines(4);
            editText.setMaxLines(6);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
            editText.setGravity(Gravity.TOP | Gravity.START);
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            layout.addView(editText);

            // Создание диалога
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(layout)
                    .setPositiveButton("Добавить", null)
                    .setNegativeButton("Отмена", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Цвета
                positiveButton.setTextColor(ContextCompat.getColor(this, R.color.my_cyan));
                negativeButton.setTextColor(ContextCompat.getColor(this, R.color.my_cyan));

                // Клик по "Добавить"
                positiveButton.setOnClickListener(view1 -> {
                    String noteText = editText.getText().toString().trim();

                    if (noteText.isEmpty()) {
                        editText.setError("Введите текст заметки");
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());

                        // Сохраняем заметку
                        databaseHelper.saveNote(currentDate, noteText, "Note", "note");

                        refreshList();
                        dialog.dismiss();
                    }
                });
            });

            dialog.show();
        });



        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void refreshList() {
        databaseHelper = new DatabaseHelper(this);

        allNotes.clear();
        filteredNotes.clear();

        Cursor cursor = databaseHelper.getNoteList(); // Получаем из БД

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String action = cursor.getString(cursor.getColumnIndexOrThrow("action"));
            allNotes.add(new Notes(id, timestamp, name, type, action));
        }

        cursor.close();

        // ✅ Сортировка по дате и времени
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        allNotes.sort((note1, note2) -> {
            try {
                Date date1 = sdf.parse(note1.getTriggerAtMillis());
                Date date2 = sdf.parse(note2.getTriggerAtMillis());
                return date2.compareTo(date1); // от новых к старым
            } catch (Exception e) {
                return 0; // если ошибка — не сортируем
            }
        });

        filteredNotes.addAll(allNotes);
    }


    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Все", "Доход", "Расход", "заметки"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterNotes(searchEditText.getText().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterNotes(String query) {
        filteredNotes.clear();
        for (Notes note : allNotes) {
            boolean matchesType = filterSpinner.getSelectedItemPosition() == 0
                    || (filterSpinner.getSelectedItemPosition() == 1 && note.getType().equals("income"))
                    || (filterSpinner.getSelectedItemPosition() == 2 && note.getType().equals("Spent"))
                    || (filterSpinner.getSelectedItemPosition() == 3 && note.getType().equals("Note"));

            if (matchesType && note.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredNotes.add(note);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
