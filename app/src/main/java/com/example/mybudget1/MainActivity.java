package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private int currentDay;
    private TextView selectedDayText;
    private int currentMonthOffset;
    private int currentDayIndex;
    private Button btnNewSpent;
    private Button otherSettings;
    private ImageButton btnRefresh;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewSpent = findViewById(R.id.btnNewSpent);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnNewSpent.setOnClickListener(v -> newSpent(this));
        btnRefresh.setOnClickListener(v ->updateAdapter());

        viewPager = findViewById(R.id.viewPager);
        currentDay = getCurrentDay();

        selectedDayText = findViewById(R.id.selectedDayText);
        currentMonthOffset = 0; // 0 = текущий месяц
        currentDayIndex = getCurrentDay() - 1;

        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem(currentDay - 1, false);

        // Обновляем текст при смене страницы
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectedDayText.setText("Day " + (position + 1));
            }
        });


        otherSettings = findViewById(R.id.otherbtn);
        otherSettings.setOnClickListener(v -> goToSettings());
    }

    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void newSpent(MainActivity mainActivity) {
        // Создаем EditText для каждого поля
        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        final EditText spent = new EditText(this);
        final EditText day = new EditText(this);

        // Устанавливаем подсказки для каждого поля
        name.setHint("название траты");
        spent.setHint("сумма");
        day.setHint("день (по умолчанию сегодня)");

        // Создаем контейнер для EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16); // Добавляем отступы для лучшего восприятия
        layout.addView(name);
        layout.addView(spent);
        layout.addView(day);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите данные")
                .setView(layout)  // Устанавливаем общий контейнер в диалог
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nameData = name.getText().toString().trim(); // Убираем лишние пробелы
                        String spentDataStr = spent.getText().toString().trim();
                        String dayDataStr = day.getText().toString().trim();

                        // Если день не указан, ставим текущий день
                        int dayData = dayDataStr.isEmpty() ? getCurrentDay() : Integer.parseInt(dayDataStr);

                        // Если название пустое, ставим "трата"
                        if (nameData.isEmpty()) {
                            nameData = "трата";
                        }

                        // Если сумма пустая или равна нулю, не сохраняем данные
                        if (spentDataStr.isEmpty() || Integer.parseInt(spentDataStr) == 0) {
                            dialog.dismiss(); // Закрываем диалог, если сумма = 0 или пустая
                            return;
                        }

                        int spentData = Integer.parseInt(spentDataStr); // Преобразуем строку в число

                        // Вставляем данные в базу
                        DatabaseHelper databaseHelper = new DatabaseHelper(mainActivity);
                        databaseHelper.insertData(dayData, nameData, spentData, 0);

                        //updateFragment();
                        updateAdapter();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    public void updateAdapter() {
        int daysInMonth = getDaysInMonth(currentMonthOffset);
        viewPager.setAdapter(new DayAdapter(this, daysInMonth, currentMonthOffset));
        viewPager.setCurrentItem(currentDay - 1, false);  // Устанавливаем нужный день после обновления
    }

    public void createNewDaySpenting() {
        String name = "test";
        int spent = 1000;
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.insertData(Calendar.DAY_OF_MONTH, name, spent, 0);
    }

    private int getDaysInMonth(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}


class DayAdapter extends FragmentStateAdapter {
    private final int daysInMonth;
    private final int monthOffset; // -1 (предыдущий месяц), 0 (текущий), 1 (следующий)

    public DayAdapter(@NonNull AppCompatActivity fragmentActivity, int daysInMonth, int monthOffset) {
        super(fragmentActivity);
        this.daysInMonth = daysInMonth;
        this.monthOffset = monthOffset;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int day = position + 1; // Дни начинаются с 1
        return DayFragment.newInstance(day, monthOffset);
    }

    @Override
    public int getItemCount() {
        return daysInMonth;
    }
}





