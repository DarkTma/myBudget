package com.example.mybudget1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CheckBox;
import java.util.Collections;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private CheckBox checkBoxSortByProcent; // Новый чекбокс

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
        checkBoxSortByProcent = findViewById(R.id.checkBoxSortByProcent);

        checkBoxSortByProcent.setOnCheckedChangeListener((buttonView, isChecked) -> loadCategories());

        fileHelper = new FileHelper(this);
        databaseHelper = new DatabaseHelper(this);

        // Настройка Spinner для выбора месяца
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Текущий месяц", "прошлый месяц", "Все"});
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
                        selectedMonthOption = "last"; // Текущий и прошлый
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
            case "last":
                categoryItems = fileHelper.getCategoriesWithPrices(2);
                break;
            case "all":
                categoryItems = fileHelper.getCategoriesWithPrices(3);
                break;
        }

        // Если чекбокс включен - сортируем по проценту
        if (checkBoxSortByProcent.isChecked()) {
            Collections.sort(categoryItems, new Comparator<CategoryItem>() {
                @Override
                public int compare(CategoryItem c1, CategoryItem c2) {
                    return Integer.compare(c2.getProcent(), c1.getProcent());
                }
            });
        }

        adapter = new CategoryAdapter(this, categoryItems, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(int categoryId) {
                showEditCategoryDialog(categoryId);
            }

            @Override
            public void onDelete(int categoryId) {
                showDeleteConfirmationDialog(categoryId);
            }
        });

        listViewCategories.setAdapter(adapter);
    }

    // Диалог для добавления новой категории
    private void showAddCategoryDialog() {

        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint("Название категории");
        name.setPadding(0, 30, 0, 10); // Добавляем больше отступов
        name.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
        name.setLayoutParams(nameParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final android.widget.EditText input = new android.widget.EditText(this);
        builder.setView(input);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите название</font>"));
        builder.setView(layout);
        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            String newCategory = name.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                // Добавляем категорию в файл
                fileHelper.addCategoryToFile(newCategory);
                // Обновляем список категорий
                categoryItems.add(new CategoryItem(categoryItems.size(), newCategory, 0 , 0));
                adapter.notifyDataSetChanged();
                Toast.makeText(CategoriesActivity.this, "Категория добавлена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoriesActivity.this, "Введите название категории", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
        dialog.show();
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
