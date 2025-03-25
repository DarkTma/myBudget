package com.example.mybudget1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesActivity extends AppCompatActivity {

    private List<ExpenseData> expenses;
    private List<ExpenseData> filteredExpenses;
    private DatabaseHelper dbHelper;
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton buttonBackFromExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        buttonBackFromExpenses = findViewById(R.id.buttonBackFromExpenses);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerViewExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        buttonBackFromExpenses.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(ExpensesActivity.this, CategoriesActivity.class);
            startActivity(intentGoBack);
        });

        // Получаем category_id из Intent
        int categoryId = getIntent().getIntExtra("category_id", -1);
        if (categoryId == -1) {
            Toast.makeText(this, "Ошибка: категория не выбрана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Получаем траты по выбранной категории
        expenses = dbHelper.getExpensesByCategory(categoryId);
        filteredExpenses = new ArrayList<>(expenses);

        // Сортируем список по дате
        sortExpensesByDate(filteredExpenses);

        // Устанавливаем адаптер
        adapter = new ExpenseAdapter(this, filteredExpenses);
        recyclerView.setAdapter(adapter);

        // Настраиваем поиск
        setupSearch();
    }

    // Сортировка списка по дате (от новых к старым)
    private void sortExpensesByDate(List<ExpenseData> list) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Collections.sort(list, (e1, e2) -> {
            try {
                Date date1 = dateFormat.parse(e1.getDate());
                Date date2 = dateFormat.parse(e2.getDate());
                // Сортировка по убыванию (новые элементы наверху)
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    // Настройка поисковой строки
    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExpenses(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExpenses(newText);
                return true;
            }
        });
    }

    // Фильтрация списка по запросу
    private void filterExpenses(String query) {
        query = query.toLowerCase(Locale.getDefault());
        filteredExpenses.clear();

        if (TextUtils.isEmpty(query)) {
            filteredExpenses.addAll(expenses);
        } else {
            for (ExpenseData expense : expenses) {
                if (expense.getDate().contains(query) ||
                        expense.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredExpenses.add(expense);
                }
            }
        }

        // Сортировка после фильтрации
        sortExpensesByDate(filteredExpenses);
        adapter.notifyDataSetChanged();
    }
}
