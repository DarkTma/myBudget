package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MonthListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MonthAdapter adapter;
    private ImageButton btnBack;
    private List<MonthData> monthDataList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_month_list);

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Инициализация DatabaseHelper
            databaseHelper = new DatabaseHelper(this);

            // Получаем данные о месяцах (список объектов MonthData)
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            monthDataList = databaseHelper.getMonthData(db);

            // Устанавливаем адаптер для RecyclerView
            adapter = new MonthAdapter(this ,monthDataList);  // Передаем список MonthData
            recyclerView.setAdapter(adapter);

            btnBack = findViewById(R.id.buttonBackforMonths);

            btnBack.setOnClickListener(v -> {
                Intent intentGoBack = new Intent(MonthListActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(MonthListActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }


}
