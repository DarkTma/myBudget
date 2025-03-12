package com.example.mybudget1;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MonthDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MonthDetailAdapter adapter;
    private ImageButton btnback;
    private List<MonthDetailData> monthDetailDataList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_detail);

        recyclerView = findViewById(R.id.recyclerViewMonthDetails);
        btnback = findViewById(R.id.buttonBackdetails);

        btnback.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(MonthDetailActivity.this, MonthListActivity.class);
            startActivity(intentGoBack);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Получаем название таблицы месяца
        String prevMonthTable = getIntent().getStringExtra("monthName");

        // Получаем данные из базы
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        monthDetailDataList = databaseHelper.getMonthDetailData(db, prevMonthTable);

        // Устанавливаем адаптер для RecyclerView
        adapter = new MonthDetailAdapter(monthDetailDataList);
        recyclerView.setAdapter(adapter);
    }
}

