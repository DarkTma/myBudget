package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FileHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "budget.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";

    private Context context;

    public FileHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT UNIQUE)";
        db.execSQL(CREATE_TABLE);

        // Вставка дефолтной категории
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, "прочее");
        db.insert(TABLE_CATEGORIES, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    public boolean addCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());

        long result = db.insert(TABLE_CATEGORIES, null, values);
        return result != -1;
    }

    public List<String> getAllCategoryNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories", null);
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return names;
    }

    public int ensureCategoryExists(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Проверка существования категории
        Cursor cursor = db.query(
                "categories",
                new String[]{"id"},
                "name = ?",
                new String[]{categoryName},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            int existingId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
            return existingId;
        }

        cursor.close();

        // Если не существует — добавляем
        ContentValues values = new ContentValues();
        values.put("name", categoryName);
        long newId = db.insert("categories", null, values);

        return (int) newId;
    }

    public boolean removeCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CATEGORIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean updateCategoryName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName.trim());
        int rows = db.update(TABLE_CATEGORIES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_NAME}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }


    // Функция для получения имени категории по её ID
    public String getCategoryNameById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_NAME}, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }

        cursor.close();
        return "Неизвестная категория";  // Возвращаем стандартное значение, если категория не найдена
    }


    public CategoryItem getAllCategoryById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_ID, COLUMN_NAME}, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(0);
            String name = cursor.getString(1);

            // Получаем сумму трат и процент
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            double price = 1;
            int allprice = 1;
            int percent = 1;

            cursor.close();
            return new CategoryItem(categoryId, name, price, percent);
        }

        cursor.close();
        return null; // или можно вернуть специальный объект "неизвестной категории"
    }

    public List<CategoryItem> getCategoriesWithPrices(Context c, int userId) {
        List<CategoryItem> categories = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(c);
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_ID, COLUMN_NAME}, null, null, null, null, COLUMN_ID + " ASC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double price = dbHelper.getAllExpenseByCategory(id, userId);
                int allprice = dbHelper.getAllExpense(userId);
                int percent = (allprice != 0) ? (int) ((price * 100) / allprice) : 0;
                categories.add(new CategoryItem(id, name, price, percent));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public int getCreditCategory() {
        SQLiteDatabase db = this.getWritableDatabase();
        int categoryId = -1;
        String categoryName = "кредиты";

        // Сначала пробуем найти ID по имени
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{"id"},
                "name = ?",
                new String[]{categoryName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        } else {
            // Категория не найдена — создаём
            ContentValues values = new ContentValues();
            values.put("name", categoryName);
            long id = db.insert(TABLE_CATEGORIES, null, values);
            if (id != -1) {
                categoryId = (int) id;
            }
        }

        return categoryId;
    }


    // Функция для получения ID категории по имени
    public int getCategoryIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_ID}, COLUMN_NAME + " = ?",
                new String[]{name.trim()}, null, null, null);

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }

        cursor.close();
        return -1;  // Если категория не найдена, возвращаем -1
    }


    public boolean updateCategory(int categoryId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);

        int rowsAffected = db.update("categories", values, "id = ?", new String[]{String.valueOf(categoryId)});
        return rowsAffected > 0;
    }
}
