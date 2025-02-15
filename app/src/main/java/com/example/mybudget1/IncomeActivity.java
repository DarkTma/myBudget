package com.example.mybudget1;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class IncomeActivity extends AppCompatActivity {

    private ListView listViewIncome;
    private IncomeAdapter adapter;
    private ArrayList<IncomeItem> incomeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        listViewIncome = findViewById(R.id.listViewIncome);

        // Заполняем список тестовыми данными
        incomeList = new ArrayList<>();
        incomeList.add(new IncomeItem("Зарплата", 100000, "10.02.2024"));
        incomeList.add(new IncomeItem("Фриланс", 50000, "15.02.2024"));
        incomeList.add(new IncomeItem("Подарок", 20000, "20.02.2024"));

        // Подключаем адаптер
        adapter = new IncomeAdapter(this, incomeList);
        listViewIncome.setAdapter(adapter);
    }
}
