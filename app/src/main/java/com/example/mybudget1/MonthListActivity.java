package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MonthListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MonthAdapter adapter;
    private Button btnBack;
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
            monthDataList = databaseHelper.getMonthData(db);  // Здесь мы получаем список MonthData

            // Устанавливаем адаптер для RecyclerView
            adapter = new MonthAdapter(monthDataList);  // Передаем список MonthData
            recyclerView.setAdapter(adapter);

            btnBack = findViewById(R.id.buttonBackMonths);

            btnBack.setOnClickListener(v -> {
                Intent intentGoBack = new Intent(MonthListActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            });
    }

    public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

        private List<MonthData> monthDataList;

        public MonthAdapter(List<MonthData> monthDataList) {
            this.monthDataList = monthDataList;
        }

        @Override
        public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month, parent, false);
            return new MonthViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MonthViewHolder holder, int position) {
            MonthData monthData = monthDataList.get(position);
            holder.monthName.setText(monthData.getMonthName());
            holder.income.setText("Income: " + monthData.getIncome());
            holder.spent.setText("Spent: " + monthData.getSpent());
        }

        @Override
        public int getItemCount() {
            return monthDataList.size();
        }

        public class MonthViewHolder extends RecyclerView.ViewHolder {
            TextView monthName, income, spent;

            public MonthViewHolder(View itemView) {
                super(itemView);
                monthName = itemView.findViewById(R.id.monthName);
                income = itemView.findViewById(R.id.income);
                spent = itemView.findViewById(R.id.spent);
            }
        }
    }
}
