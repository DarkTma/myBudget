package com.example.mybudget1;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import androidx.activity.OnBackPressedCallback;
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
    private Spinner monthSelector;
    private String selectedMonthOption = "current";
    private CheckBox checkBoxSortByProcent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        buttonBackFromCategories = findViewById(R.id.buttonBackFromCategories);
        buttonBackFromCategories.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(CategoriesActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(CategoriesActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        listViewCategories = findViewById(R.id.listViewCategories);
        btnAddCategory = findViewById(R.id.btnAddCategorie);
        monthSelector = findViewById(R.id.monthSelector);
        checkBoxSortByProcent = findViewById(R.id.checkBoxSortByProcent);

        checkBoxSortByProcent.setOnCheckedChangeListener((buttonView, isChecked) -> loadCategories());

        fileHelper = new FileHelper(this);
        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Текущий месяц", "прошлый месяц", "Все"});
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSelector.setAdapter(adapterMonth);

        monthSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        selectedMonthOption = "current";
                        break;
                    case 1:
                        selectedMonthOption = "last";
                        break;
                    case 2:
                        selectedMonthOption = "all";
                        break;
                }
                loadCategories();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (selectedMonthOption) {
            case "current":
                categoryItems = fileHelper.getCategoriesWithPrices(this,1);
                break;
            case "last":
                categoryItems = fileHelper.getCategoriesWithPrices(this,2);
                break;
            case "all":
                categoryItems = fileHelper.getCategoriesWithPrices(this,3);
                break;
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

    private void showAddCategoryDialog() {
        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint("Название категории");
        name.setPadding(0, 30, 0, 10);
        name.setBackgroundResource(R.drawable.edit_text_style);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 10, 0, 20);
        name.setLayoutParams(nameParams);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите название</font>"));
        builder.setView(layout);

        SpannableString positiveButtonText = new SpannableString("Добавить");
        positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString negativeButtonText = new SpannableString("Отмена");
        negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            String newCategory = name.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                if (fileHelper.addCategory(newCategory)) {
                    Toast.makeText(CategoriesActivity.this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CategoriesActivity.this, CategoriesActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Такая категория уже существует", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CategoriesActivity.this, "Введите название категории", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
    }

    private void showDeleteConfirmationDialog(final int categoryId) {
        CategoryItem category = categoryItems.get(categoryId);

        // Создаём Spinner
        Spinner spinner = new Spinner(this);
        List<String> categoryNames = fileHelper.getAllCategoryNames();
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        // Устанавливаем "other" как выбранный по умолчанию
        int defaultPosition = categoryNames.indexOf("other");
        if (defaultPosition >= 0) {
            spinner.setSelection(defaultPosition);
        }

        // Оборачиваем Spinner в Layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = 32;
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(spinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удалить категорию?");
        builder.setMessage("Выберите категорию для переноса расходов:");

        builder.setView(layout);

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            String selectedCategoryName = (String) spinner.getSelectedItem();
            int selectedCategoryId = fileHelper.getCategoryIdByName(selectedCategoryName);

            List<ExpenseData> expenses = databaseHelper.getExpensesByCategory(category.getId());
            for (ExpenseData expense : expenses) {
                databaseHelper.updateExpenseCategory(expense.getId(), selectedCategoryId);
            }

            boolean isDeleted = fileHelper.removeCategory(category.getId());
            if (isDeleted) {
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


    private void showEditCategoryDialog(final int categoryId) {
        CategoryItem category = categoryItems.get(categoryId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить категорию");

        final EditText input = new EditText(this);
        input.setText(category.getName());
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newCategoryName = input.getText().toString().trim();
            if (!newCategoryName.isEmpty()) {
                boolean isUpdated = fileHelper.updateCategory(category.getId(), newCategoryName);
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
