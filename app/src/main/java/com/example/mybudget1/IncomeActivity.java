package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class IncomeActivity extends AppCompatActivity {

    private ListView listViewIncome;
    private IncomeAdapter adapter;
    private ArrayList<IncomeItem> incomeList;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        listViewIncome = findViewById(R.id.listViewIncome);
        btnBack = findViewById(R.id.buttonBackFromIncome);

        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(IncomeActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });


        // Заполняем список тестовыми данными
        incomeList = new ArrayList<>();
        Cursor income = databaseIncome.getIncomeList();
        if (income != null && income.moveToFirst()) {
            do {
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                int incomeNum = income.getInt(income.getColumnIndexOrThrow("income"));
                String date = income.getString(income.getColumnIndexOrThrow("incomeday"));
                String once = income.getString(income.getColumnIndexOrThrow("onceincome"));
                boolean x = false;
                if (once.equals("1")) {
                    x = true;
                }
                incomeList.add(new IncomeItem(name, incomeNum, date, x));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new IncomeAdapter(this, incomeList);
        listViewIncome.setAdapter(adapter);
    }
}
