package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ExpensesActivity extends AppCompatActivity {

    private ListView listViewExpenses;
    private List<ExpenseData> expenses;
    private DatabaseHelper dbHelper;

    private ImageButton buttonBackFromExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        buttonBackFromExpenses = findViewById(R.id.buttonBackFromExpenses);
        buttonBackFromExpenses.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(ExpensesActivity.this, CategoriesActivity.class);
            startActivity(intentGoBack);
        });

        listViewExpenses = findViewById(R.id.listViewExpenses);
        dbHelper = new DatabaseHelper(this);

        // Получаем category_id из Intent
        int categoryId = getIntent().getIntExtra("category_id", -1);
        if (categoryId == -1) {
            Toast.makeText(this, "Ошибка: категория не выбрана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Получаем трату по выбранной категории
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        expenses = databaseHelper.getExpensesByCategory(categoryId);

        // Настроим адаптер для отображения трат
        ArrayAdapter<ExpenseData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenses);
        listViewExpenses.setAdapter(adapter);
    }
}
