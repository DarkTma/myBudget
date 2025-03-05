package com.example.mybudget1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MonthDetailsActivity extends AppCompatActivity {

    private TextView monthDetailsText;
    private String month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_details);

        monthDetailsText = findViewById(R.id.monthDetailsText);

        // Получаем выбранный месяц
        month = getIntent().getStringExtra("month");

        // Загружаем подробности по месяцу
        loadMonthDetails(month);
    }

    private void loadMonthDetails(String month) {
        // Тут нужно получить и отобразить подробности о доходах и расходах по дням для месяца
        String details = "Details for " + month;
        details += "\n\nIncome for the month: " + getIncomeForMonth(month);
        details += "\nSpent for the month: " + getSpentForMonth(month);
        monthDetailsText.setText(details);
    }

    // Метод для получения дохода для месяца (реализуйте в вашей базе)
    private double getIncomeForMonth(String month) {
        // Запрос в базу данных для получения дохода
        return 1000.0; // Пример
    }

    // Метод для получения расходов для месяца (реализуйте в вашей базе)
    private double getSpentForMonth(String month) {
        // Запрос в базу данных для получения расходов
        return 500.0; // Пример
    }
}
