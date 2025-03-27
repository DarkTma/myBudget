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

    public static String getCategoryById(Context context, int categoryId) {
        FileHelper fileHelper = new FileHelper(context);
        List<String> categories = fileHelper.readCategoriesFromFile();

        if (categoryId >= 0 && categoryId < categories.size()) {
            return categories.get(categoryId);
        }
        return "Неизвестная категория"; // Если ID не найден
    }


    public List<CategoryItem> getCategoriesWithPrices(int i) {
        List<CategoryItem> categories = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(context); // Создаём экземпляр DatabaseHelper
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            int id = 0; // Условный ID для категорий

            while ((line = reader.readLine()) != null) {
                String[] categoryArray = line.split("-");
                for (String category : categoryArray) {
                    if (!category.isEmpty()) {
                        int price = databaseHelper.getAllExpenseByCategory(id , i);
                        int allprice = databaseHelper.getAllExpense(i);
                        int procent = (allprice != 0) ? (price * 100) / allprice : 0;
                        categories.add(new CategoryItem(id, category, price , procent));
                        id++;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultCategoriesFile();
            categories.add(new CategoryItem(0, "other", 0 , 0));
        }
        return categories;
    }

    public boolean updateCategoryName(int categoryId, String newCategoryName) {
        List<String> categories = readCategoriesFromFile();
        if (categoryId >= 0 && categoryId < categories.size()) {
            // Обновляем название категории
            categories.set(categoryId, newCategoryName);

            try {
                // Перезаписываем файл с обновленными категориями
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
