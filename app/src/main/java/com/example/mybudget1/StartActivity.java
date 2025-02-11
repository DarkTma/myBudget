package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {
    private LinearLayout menuLayout;
    private Button btnOpenMenu, btnCloseMenu, btnExpandList;
    private ListView listView;
    private WeekItemAdapter adapter;
    private List<String> dataList;
    private boolean isExpanded = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        // Инициализация элементов
        menuLayout = findViewById(R.id.menuLayout);
        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnCloseMenu = findViewById(R.id.btnCloseMenu);
        btnExpandList = findViewById(R.id.btnExpandList);
        listView = findViewById(R.id.listView);

        // Данные для ListView
        List<WeekItem> dataList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int getCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentDayIndex = getCurrentDay;
        int prevDay = currentDayIndex - 1;
        int nextDay = currentDayIndex + 1;

        calendar.set(Calendar.DAY_OF_MONTH, currentDayIndex);
        SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE", new Locale("ru"));
        String currentDayName = sdf1.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, prevDay);
        SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE", new Locale("ru"));
        String prevDayName = sdf2.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, nextDay);
        SimpleDateFormat sdf3 = new SimpleDateFormat("EEEE", new Locale("ru"));
        String nextDayName = sdf3.format(calendar.getTime());

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        int prevDaySpent = databaseHelper.getDoneSpents(prevDay, prevDay);
        int prevDayMustDo = databaseHelper.getAllSpents(prevDay, prevDay);
        int todaySpent = databaseHelper.getDoneSpents(currentDayIndex, currentDayIndex);
        int todayMustDo = databaseHelper.getAllSpents(currentDayIndex, currentDayIndex);
        int nextDaySpent = databaseHelper.getDoneSpents(nextDay, nextDay);
        int nextDayMustDo = databaseHelper.getAllSpents(nextDay, nextDay);


        int prevDayProcent = getProcent(prevDay , prevDaySpent);
        int todayProcent = getProcent(currentDayIndex , todaySpent);
        int nextDayProcent = getProcent(nextDay , nextDaySpent);


        dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "выпалнено: " + prevDayProcent + "%", "из: " + prevDayMustDo + "₽"));
        dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "выпалнено: " + todayProcent + "%" , "из: " + todayMustDo + "₽"));
        dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "выпалнено: " + nextDayProcent + "%" , "из: " + nextDayMustDo + "₽"));


        adapter = new WeekItemAdapter(this, dataList);
        listView.setAdapter(adapter);

        // Открыть меню
        btnOpenMenu.setOnClickListener(v -> menuLayout.setVisibility(View.VISIBLE));

        // Закрыть меню
        btnCloseMenu.setOnClickListener(v -> menuLayout.setVisibility(View.GONE));

        // Расширение списка
        btnExpandList.setOnClickListener(v -> {
            if (isExpanded) {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "выпалнено: " + prevDayProcent + "%", "из: " + prevDayMustDo + "₽"));
                dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "выпалнено: " + todayProcent + "%" , "из: " + todayMustDo + "₽"));
                dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "выпалнено: " + nextDayProcent + "%" , "из: " + nextDayMustDo + "₽"));

                isExpanded = false;
                listView.getLayoutParams().height -= 700; // Увеличиваем высоту
            } else {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                int day = DayAdapter.getStartOfWeek();
                String[] weekDays = {"понедельник", "вторник", "среда", "четверг" , "пятница", "суббота", "воскресение"};
                for (int i = 0; i < 7; i++) {
                    dataList.add(new WeekItem(weekDays[i] , "потрачено: " + databaseHelper.getDoneSpents(day+i, day+i)+"₽",
                            "выполнено: " + getProcent(day+i, databaseHelper.getDoneSpents(day+i, day+i))+"%" ,
                            "из: " + databaseHelper.getAllSpents(day+i, day+i) + "₽"));
                }
                listView.getLayoutParams().height += 700; // Возвращаем высоту
                isExpanded = true;
            }
            adapter.notifyDataSetChanged();
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный элемент списка
                WeekItem selectedItem = (WeekItem) parent.getItemAtPosition(position);

                // Создаем Intent для перехода на новую активность
                Intent intent = new Intent(StartActivity.this, MainActivity.class);

                // Передаем данные в новую активность
                intent.putExtra("day", position);
                intent.putExtra("dayName", selectedItem.getDayName());
                intent.putExtra("spent", selectedItem.getSpent());
                intent.putExtra("spentProcent", selectedItem.getSpentProcent());

                // Запускаем новую активность
                startActivity(intent);
                finish();
            }
        });
    }

    private int getProcent(int day , int a){
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        int b = databaseHelper.getNotDoneSpents(day, day);
        if (a + b > 0) {
            double resultD = (a / (double)(a + b)) * 100;
            int result = (int) Math.round(resultD);
            return result;
        } else {
            if (databaseHelper.getAllSpents(day, day) == 0){
                return 100;
            } else {
                return 0;
            }
        }
    }


}

