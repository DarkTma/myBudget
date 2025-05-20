package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DayActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<WeekItem> dataList;
    private WeekItemAdapter adapter;
    private boolean isExpanded = false;

    public CursData curs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        listView = findViewById(R.id.listView);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        curs = CursHelper.getCursData(databaseIncome.getCurs());

        ImageButton btnBack = findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(DayActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        MenuHelper.setupMenu(this);

        Button btnOpenMenu = findViewById(R.id.btnOpenMenu);
        View dimLayer = findViewById(R.id.dimLayer);
        LinearLayout menuLayout = findViewById(R.id.menuLayout);

        btnOpenMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.VISIBLE);
            dimLayer.setVisibility(View.VISIBLE);
        });

        // Закрытие по клику на слой
        dimLayer.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        dataList = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("0.##");

        Calendar calendar = Calendar.getInstance();
        int getCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);

        int day = DayAdapter.getStartOfWeek();
        int todayCalendarDay = calendar.get(Calendar.DAY_OF_WEEK);

        String[] weekDays = {"понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресение"};

        // todayCalendarDay в Java начинается с Воскресенья=1, делаем сдвиг, чтобы понедельник = 0
        int todayIndex = (todayCalendarDay + 5) % 7;

        for (int i = 0; i < 7; i++) {
            String dayName = weekDays[i];
            if (i == todayIndex) {
                dayName += " (сегодня)";
            }

            dataList.add(new WeekItem(dayName,
                    "потрачено: " + df.format(databaseHelper.getDoneSpents(day + i, day + i) * curs.rate) + curs.symbol,
                    "из: " + df.format(databaseHelper.getAllSpents(day + i, day + i) * curs.rate) + curs.symbol));
        }

        // Инициализация адаптера должна быть после заполнения dataList
        adapter = new WeekItemAdapter(this, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            WeekItem selectedItem = (WeekItem) parent.getItemAtPosition(position);

            Intent intent = new Intent(DayActivity.this, MainActivity.class);

            // Если понадобится разветвление на расширенный список — раскомментируй и допиши логику
            intent.putExtra("day", position);
            intent.putExtra("isexpented", "true");

            startActivity(intent);
            finish();
        });
    }
}
