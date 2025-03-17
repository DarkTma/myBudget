package com.example.mybudget1;

import android.content.Context;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    private Context context;
    private static final String FILE_NAME = "categories.txt";

    public FileHelper(Context context) {
        this.context = context;
    }

    // Чтение категорий из файла
    public List<String> readCategoriesFromFile() {
        List<String> categories = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] categoryArray = line.split("-");
                for (String category : categoryArray) {
                    if (!category.isEmpty()) {
                        categories.add(category);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultCategoriesFile();
            categories.add("other");  // Добавляем дефолтную категорию, если файл не существует
        }
        return categories;
    }

    // Добавление новой категории в файл
    public void addCategoryToFile(String newCategory) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write("-" + newCategory);  // Добавляем категорию с дефисом
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Удаление категории из файла
    public boolean removeCategoryFromFile(String categoryToDelete) {
        List<String> categories = readCategoriesFromFile();
        if (categories.remove(categoryToDelete)) {
            // Перезаписываем файл с обновленным списком категорий
            try {
                FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                for (String category : categories) {
                    writer.write(category + "-");
                }
                writer.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Создание файла с дефолтной категорией "other", если файл не существует
    private void createDefaultCategoriesFile() {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write("other");  // Записываем дефолтную категорию в файл
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
