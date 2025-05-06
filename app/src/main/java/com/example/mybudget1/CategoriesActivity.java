package com.example.mybudget1;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private ListView listViewCategories;
    private Button btnAddCategory;
    private Spinner monthSelector;
    private CheckBox checkBoxSortByProcent;

    private CategoryAdapter adapter;
    private List<CategoryItem> categoryItems;

    private FileHelper fileHelper;
    private DatabaseHelper databaseHelper;

    private String selectedMonthOption = "current";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        initViews();
        setupBackNavigation();
        setupMonthSelector();
        setupAddCategoryButton();

        fileHelper = new FileHelper(this);
        databaseHelper = new DatabaseHelper(this);

        loadCategories();
    }

    private void initViews() {
        listViewCategories = findViewById(R.id.listViewCategories);
        btnAddCategory = findViewById(R.id.btnAddCategorie);
        monthSelector = findViewById(R.id.monthSelector);
        checkBoxSortByProcent = findViewById(R.id.checkBoxSortByProcent);
    }

    private void setupBackNavigation() {
        ImageButton buttonBack = findViewById(R.id.buttonBackFromCategories);
        buttonBack.setOnClickListener(v -> startActivity(new Intent(this, StartActivity.class)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(CategoriesActivity.this, StartActivity.class));
            }
        });
    }

    private void setupMonthSelector() {
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Текущий месяц", "Прошлый месяц", "Все"});
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelector.setAdapter(adapterMonth);

        monthSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        selectedMonthOption = "last";
                        break;
                    case 2:
                        selectedMonthOption = "all";
                        break;
                    case 0:
                    default:
                        selectedMonthOption = "current";
                        break;
                }

                loadCategories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        checkBoxSortByProcent.setOnCheckedChangeListener((buttonView, isChecked) -> loadCategories());
    }

    private void setupAddCategoryButton() {
        btnAddCategory.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Название категории");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout layout = new LinearLayout(this);
            layout.setPadding(50, 30, 50, 10);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(input);

            new AlertDialog.Builder(this)
                    .setTitle("Новая категория")
                    .setView(layout)
                    .setPositiveButton("Добавить", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            if (fileHelper.addCategory(name)) {
                                Toast.makeText(this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                Toast.makeText(this, "Категория уже существует", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void loadCategories() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        if ("last".equals(selectedMonthOption)) {
            categoryItems = fileHelper.getCategoriesWithPrices(this, 2);
        } else if ("all".equals(selectedMonthOption)) {
            categoryItems = fileHelper.getCategoriesWithPrices(this, 3);
        } else {
            categoryItems = fileHelper.getCategoriesWithPrices(this, 1);
        }


        if (checkBoxSortByProcent.isChecked()) {
            Collections.sort(categoryItems, Comparator.comparingInt(CategoryItem::getProcent).reversed());
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

    private void showDeleteConfirmationDialog(int categoryId) {
        CategoryItem category = fileHelper.getAllCategoryById(categoryId);
        List<String> categoryNames = fileHelper.getAllCategoryNames();

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        int defaultPosition = categoryNames.indexOf("прочее");
        if (defaultPosition >= 0) {
            spinner.setSelection(defaultPosition);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(spinner);

        new AlertDialog.Builder(this)
                .setTitle("Удалить категорию?")
                .setMessage("Выберите категорию для переноса трат:")
                .setView(layout)
                .setPositiveButton("Удалить", (dialog, which) -> {
                    int newCategoryId = fileHelper.getCategoryIdByName((String) spinner.getSelectedItem());

                    List<ExpenseData> expenses = databaseHelper.getExpensesByCategory(categoryId);
                    for (ExpenseData expense : expenses) {
                        databaseHelper.updateExpenseCategory(expense.getId(), newCategoryId);
                    }

                    if (fileHelper.removeCategory(categoryId)) {
                        Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditCategoryDialog(int categoryId) {
        CategoryItem category = fileHelper.getAllCategoryById(categoryId);

        EditText input = new EditText(this);
        input.setText(category.getName());

        new AlertDialog.Builder(this)
                .setTitle("Изменить категорию")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        if (fileHelper.updateCategory(categoryId, newName)) {
                            Toast.makeText(this, "Категория обновлена", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        } else {
                            Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
