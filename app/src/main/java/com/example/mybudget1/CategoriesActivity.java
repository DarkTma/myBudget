package com.example.mybudget1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private ListView listViewCategories;
    private Button btnAddCategory;
    private CategoryAdapter adapter;
    private List<CategoryItem> categoryItems;
    private FileHelper fileHelper;
    private DatabaseHelper databaseHelper;
    private ImageButton buttonBackFromCategories;
    private Spinner monthSelector; // Селектор для месяца
    private String selectedMonthOption = "current"; // По умолчанию выбран текущий месяц

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        // Инициализация UI компонентов
        buttonBackFromCategories = findViewById(R.id.buttonBackFromCategories);
        buttonBackFromCategories.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(CategoriesActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        listViewCategories = findViewById(R.id.listViewCategories);
        btnAddCategory = findViewById(R.id.btnAddCategorie);
        monthSelector = findViewById(R.id.monthSelector); // Инициализация Spinner

        fileHelper = new FileHelper(this);
        databaseHelper = new DatabaseHelper(this);

        // Настройка Spinner для выбора месяца
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Текущий месяц", "Текущий и прошлый", "Все"});
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelector.setAdapter(adapterMonth);

        monthSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        selectedMonthOption = "current"; // Текущий месяц
                        break;
                    case 1:
                        selectedMonthOption = "last 2"; // Текущий и прошлый
                        break;
                    case 2:
                        selectedMonthOption = "all"; // Все месяцы
                        break;
                }
                loadCategories(); // Загружаем категории в зависимости от выбранного месяца
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Если ничего не выбрано, ничего не меняем
            }
        });

        // Получаем категории с ценами
        loadCategories();

        // Добавление новой категории
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    // Метод для загрузки категорий в зависимости от выбранного месяца
    private void loadCategories() {
        switch (selectedMonthOption) {
            case "current":
                categoryItems = fileHelper.getCategoriesWithPrices(1);
                break;
            case "last 2":
                categoryItems = fileHelper.getCategoriesWithPrices(2);
                break;
            case "all":
                categoryItems = fileHelper.getCategoriesWithPrices(3);
                break;
        }

        // Создаем адаптер и передаем обработчики для редактирования и удаления
        adapter = new CategoryAdapter(this, categoryItems, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(int categoryId) {
                // Логика редактирования категории
                showEditCategoryDialog(categoryId);
            }

            @Override
            public void onDelete(int categoryId) {
                // Логика удаления категории
                showDeleteConfirmationDialog(categoryId);
            }
        });

        listViewCategories.setAdapter(adapter);
    }

    // Диалог для добавления новой категории
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить категорию");

        final android.widget.EditText input = new android.widget.EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String newCategory = input.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                // Добавляем категорию в файл
                fileHelper.addCategoryToFile(newCategory);
                // Обновляем список категорий
                categoryItems.add(new CategoryItem(categoryItems.size(), newCategory, 0));
                adapter.notifyDataSetChanged();
                Toast.makeText(CategoriesActivity.this, "Категория добавлена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoriesActivity.this, "Введите название категории", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // Диалог для подтверждения удаления категории
    private void showDeleteConfirmationDialog(final int categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удалить категорию?");
        builder.setMessage("Вы уверены, что хотите удалить эту категорию?");

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            CategoryItem category = categoryItems.get(categoryId);

            // Перемещаем все расходы из удаляемой категории в "other"
            List<ExpenseData> expenses = databaseHelper.getExpensesByCategory(categoryId);
            int otherCategoryId = 0; // ID для категории "other"
            for (ExpenseData expense : expenses) {
                databaseHelper.updateExpenseCategory(category.getId(), otherCategoryId);
            }

            // Удаляем категорию из файла
            boolean isDeleted = fileHelper.removeCategoryFromFile(category.getName());
            if (isDeleted) {
                // Убираем категорию из списка и обновляем ListView
                categoryItems.remove(categoryId);
                adapter.notifyDataSetChanged();
                Toast.makeText(CategoriesActivity.this, "Категория удалена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoriesActivity.this, "Ошибка удаления категории", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // Диалог для редактирования категории
    private void showEditCategoryDialog(final int categoryId) {
        CategoryItem category = categoryItems.get(categoryId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить категорию");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(category.getName());
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newCategoryName = input.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                // Обновляем имя категории в файле
                boolean isUpdated = fileHelper.updateCategoryName(category.getId(), newCategoryName);
                if (isUpdated) {
                    category.setName(newCategoryName);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CategoriesActivity.this, "Категория изменена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CategoriesActivity.this, "Ошибка изменения категории", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CategoriesActivity.this, "Имя категории не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}
