package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;

    // Названия таблиц для хранения данных трех месяцев
    private String prevMonthTable;
    private String currentMonthTable;
    private String nextMonthTable;
//    private String stabilSpents;
//    private String income;

    // Колонки таблицы
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DONE = "isdone";
    private static final String COLUMN_SPENT = "spent";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        updateMonthTables();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, prevMonthTable);
        createTable(db, currentMonthTable);
        createTable(db, nextMonthTable);
//        createTableForMonthSpent(db, stabilSpents);
//        createTableForIncome(db , income);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + prevMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + currentMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + nextMonthTable);
        onCreate(db);
    }

    private void updateMonthTables(SQLiteDatabase db) {
        // Удаляем таблицу прошлого месяца
        db.execSQL("DROP TABLE IF EXISTS " + prevMonthTable);

        // Переименовываем текущий месяц в прошлый
        db.execSQL("ALTER TABLE " + currentMonthTable + " RENAME TO " + prevMonthTable);

        // Переименовываем следующий месяц в текущий
        db.execSQL("ALTER TABLE " + nextMonthTable + " RENAME TO " + currentMonthTable);

        // Создаем новую таблицу для нового следующего месяца
        createTable(db, nextMonthTable);
    }

    private void createTable(SQLiteDatabase db, String tableName) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAY + " INTEGER, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DONE + " BOOLEAN, " +
                COLUMN_SPENT + " INTEGER)";
        db.execSQL(createTable);
    }

    public void sbros(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + currentMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + prevMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + nextMonthTable);
        createTable(db ,currentMonthTable);
        createTable(db ,nextMonthTable);
        createTable(db ,prevMonthTable);
    }

    // Обновляем названия таблиц в зависимости от текущего месяца
    public void updateMonthTables() {
        Calendar calendar = Calendar.getInstance();
        prevMonthTable = getTableName(-1);
        currentMonthTable = getTableName(0);
        nextMonthTable = getTableName(1);
    }

    private String getTableName(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, offset);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Январь = 1, Февраль = 2...
        return "month_" + year + "_" + month;
    }

    // Метод для добавления данных
    public boolean insertData(int day, String name, int spent, int offset , boolean isDone) {
        String tableName;
        if (offset == -1) {
            tableName = prevMonthTable;
        } else if (offset == 0) {
            tableName = currentMonthTable;
        } else {
            tableName = nextMonthTable;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // Проверяем, существует ли уже такая запись
        Cursor getheredData = db.rawQuery(
                "SELECT * FROM " + tableName +
                        " WHERE " + COLUMN_DAY + " = ? AND " + COLUMN_NAME + " LIKE ?",
                new String[]{String.valueOf(day), name + "%"} // Ищем совпадения по имени
        );

        int spentData = 0;
        boolean exists = false;
        boolean waschanged = false;
        int count = 0;
        String newName = name;
        String namePred = "";
        long result = 0;

        if (getheredData.moveToFirst()) {
            spentData = getheredData.getInt(getheredData.getColumnIndexOrThrow(COLUMN_SPENT));
            exists = false;

            // Проверяем, есть ли уже счетчик в имени (например, "name(3)")
            do {
                String existingName = getheredData.getString(getheredData.getColumnIndexOrThrow(COLUMN_NAME));
                if (existingName.matches(name + "\\(\\d+\\)")  || existingName.equals(name) ) {
                    // Извлекаем число из скобок
                    if (existingName.matches(name + "\\(\\d+\\)")) {
                        String number = existingName.replaceAll("[^0-9]", "");
                        count = Math.max(count, Integer.parseInt(number));
                        count++; // Увеличиваем счетчик
                        newName = name + "(" + count + ")"; // Формируем новое имя
                        namePred = name + "(" + (count - 1) + ")";
                        waschanged = true;
                    }else {
                        count = 1;
                        newName = name + "(" + count + ")";
                    }


                    getheredData.close(); // Закрываем курсор

                    spent += spentData; // Обновляем сумму затрат

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_DAY, day);
                    contentValues.put(COLUMN_NAME, newName);
                    contentValues.put(COLUMN_SPENT, spent);
                    if (isDone){
                        contentValues.put(COLUMN_DONE, true);
                    } else {
                        contentValues.put(COLUMN_DONE, false);
                    }


                    if (waschanged){
                        result = db.update(tableName, contentValues,
                                COLUMN_DAY + " = ? AND " + COLUMN_NAME + " = ?",
                                new String[]{String.valueOf(day), namePred});
                    }else {
                        result = db.update(tableName, contentValues,
                                COLUMN_DAY + " = ? AND " + COLUMN_NAME + " = ?",
                                new String[]{String.valueOf(day), name});
                    }

                    exists = true;
                }
            } while (getheredData.moveToNext());
        }
        getheredData.close(); // Закрываем курсор

        if (!exists){
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DAY, day);
            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_SPENT, spent);
            if (isDone){
                contentValues.put(COLUMN_DONE, true);
            } else {
                contentValues.put(COLUMN_DONE, false);
            }

            result = db.insert(tableName, null, contentValues);
        }

        return result != -1;
    }

    // Метод для получения данных
    public Cursor getData(int day, int offset) {
        String tableName;
        if (offset == -1) {
            tableName = prevMonthTable;
        } else if (offset == 0) {
            tableName = currentMonthTable;
        } else {
            tableName = nextMonthTable;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tableName + " WHERE " + COLUMN_DAY + " = ?", new String[]{String.valueOf(day)});
    }

    public void deleteData(String name, int day){
        String tableName = currentMonthTable;
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = "name = ? AND day = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};
        // Выполняем удаление
        db.delete(tableName, whereClause, whereArgs);


    }

    public boolean updateData(String itemName, int currentDay, String newName, int newSpent) {
        String tableName = currentMonthTable;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int day = currentDay;
        String oldName = itemName;

        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_SPENT, newSpent);

        // Обновляем запись, где день и старое имя совпадают
        int result = db.update(
                tableName,
                contentValues,
                COLUMN_DAY + " = ? AND " + COLUMN_NAME + " = ?",
                new String[]{String.valueOf(day), oldName}
        );

        return result > 0; // Если обновлено хотя бы 1 строка, вернет true
    }

    public void setDone(String name, int day , int offset , boolean isDone){
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName;
        if (offset == -1) {
            tableName = prevMonthTable;
        } else if (offset == 0) {
            tableName = currentMonthTable;
        } else {
            tableName = nextMonthTable;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_DAY, day);
        contentValues.put(COLUMN_DONE, isDone);
        db.update(tableName, contentValues, "day = ? AND name = ?", new String[]{String.valueOf(day), name});

    }


    public int checkAllSpents(){
        String tableNameforDay = currentMonthTable;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor spenti;
        int sum = 0;
        for (int i = 0; i < 31; i++) {
            spenti = db.rawQuery("SELECT " + COLUMN_SPENT + " FROM " + tableNameforDay + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(i)});

            if (spenti.moveToFirst()) {
                sum += spenti.getInt(0);
            }

            spenti.close();
        }

        return sum;
    }

    public int getDoneSpents(int start , int end){
        String tableName = currentMonthTable;
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT SUM(spent) FROM " + tableName +
                " WHERE isdone = 1 AND day BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(start), String.valueOf(end)});
        int totalSpent = 0;

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getInt(0);
        }
        cursor.close();
        return totalSpent;
    }

    public int getNotDoneSpents(int start , int end){
        String tableName = currentMonthTable;
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT SUM(spent) FROM " + tableName +
                " WHERE isdone = 0 AND day BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(start), String.valueOf(end)});
        int totalSpent = 0;

        if (cursor.moveToFirst()) {
            totalSpent = cursor.getInt(0);
        }
        cursor.close();
        return totalSpent;
    }

    public int getAllSpents(int start, int end){
        int all = getNotDoneSpents(start , end) + getDoneSpents(start , end);

        return  all;
    }

    public void monthchanged(){
        SQLiteDatabase db = this.getWritableDatabase();
        updateMonthTables(db);
    }




}


