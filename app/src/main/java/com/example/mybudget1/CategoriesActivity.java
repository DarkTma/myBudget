package com.example.mybudget1;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private ListView listViewCategories;
    private Button btnAddCategory;
    private ArrayAdapter<String> adapter;
    private List<String> categories;
    private FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        listViewCategories = findViewById(R.id.listViewCategories);
        btnAddCategory = findViewById(R.id.btnAddCategorie);
        fileHelper = new FileHelper(this);

        // Получаем категории из файла
        categories = fileHelper.readCategoriesFromFile();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        listViewCategories.setAdapter(adapter);

        listViewCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(position);
            }
        });

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });
    }

    // Диалог для добавления новой категории
    private void showAddCategoryDialog() {
        // Создаем AlertDialog для ввода новой категории
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить категорию");

        final android.widget.EditText input = new android.widget.EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCategory = input.getText().toString().trim();
                if (!newCategory.isEmpty()) {
                    // Добавляем категорию в файл
                    fileHelper.addCategoryToFile(newCategory);
                    // Обновляем список категорий
                    categories.add(newCategory);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CategoriesActivity.this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CategoriesActivity.this, "Введите название категории", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Отмена", null);

        builder.show();
    }

    // Диалог для подтверждения удаления категории
    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удалить категорию?");
        builder.setMessage("Вы уверены, что хотите удалить эту категорию?");

        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Удаляем категорию из файла
                boolean isDeleted = fileHelper.removeCategoryFromFile(categories.get(position));
                if (isDeleted) {
                    // Убираем категорию из списка и обновляем ListView
                    categories.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CategoriesActivity.this, "Категория удалена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CategoriesActivity.this, "Ошибка удаления категории", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", null);

        builder.show();
    }
}
