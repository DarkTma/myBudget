package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper2 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finance.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MONTHLY_SPENT = "monthly_spents";
    private static final String TABLE_INCOME = "income";
    private static final String TABLE_BUDGET = "budget";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SPENTDAY = "spentday";
    private static final String COLUMN_SPENT = "monthly_spent";
    private static final String COLUMN_INCOME = "income";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_INCOMEDAY = "incomeday";
    private static final String COLUMN_ONCEINCOME = "onceincome";
    private static final String COLUMN_NEXT = "next";
    private static final String COLUMN_LASTACTIVITY = "lastactivity";
    private static final String COLUMN_BUDGET = "budget";
    private static final String COLUMN_CURS = "curs";



    public DatabaseHelper2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableMonthlySpent = "CREATE TABLE IF NOT EXISTS " + TABLE_MONTHLY_SPENT + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY + " INTEGER DEFAULT 0, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_NEXT + " TEXT, " +
                COLUMN_SPENTDAY + " INTEGER, " +
                COLUMN_COUNT + " INTEGER, " +
                COLUMN_SPENT + " INTEGER)";
        db.execSQL(createTableMonthlySpent);

        String createTableIncome = "CREATE TABLE IF NOT EXISTS " + TABLE_INCOME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_INCOME + " INTEGER, " +
                COLUMN_INCOMEDAY + " INTEGER, " +
                COLUMN_NEXT + " TEXT, " +
                COLUMN_COUNT + " INTEGER, " +
                COLUMN_ONCEINCOME + " BOOLEAN)";
        db.execSQL(createTableIncome);

        String createTableBudget = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET + " INTEGER DEFAULT 0, " +
                COLUMN_CURS + " TEXT DEFAULT 'dram', " +
                COLUMN_LASTACTIVITY + " TEXT)";
        db.execSQL(createTableBudget);

        // Вставляем дефолтную строку с бюджетом 0
        String insertDefaultRow = "INSERT INTO " + TABLE_BUDGET + " (" + COLUMN_BUDGET + ", " + COLUMN_LASTACTIVITY + ") " +
                "SELECT 0, '' WHERE NOT EXISTS (SELECT 1 FROM " + TABLE_BUDGET + ")";
        db.execSQL(insertDefaultRow);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTHLY_SPENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }



    public boolean insertMonthlySpent(String name, int spent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_SPENT, spent);
        long result = db.insert(TABLE_MONTHLY_SPENT, null, contentValues);
        return result != -1;
    }

    public boolean setIncome(int value, String name, int day, boolean once) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_NAME, name);
        contentValues2.put(COLUMN_INCOME, value);
        contentValues2.put(COLUMN_INCOMEDAY, day);
        contentValues2.put(COLUMN_ONCEINCOME, once);
        contentValues2.put(COLUMN_COUNT, 0);

        // Получаем текущую дату
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH начинается с 0
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Определяем месяц для установки COLUMN_NEXT
        int targetMonth = (currentDay > day) ? (currentMonth + 1) : currentMonth;
        int targetYear = currentYear;

        // Если месяц стал 13 (т.е. следующий год), исправляем
        if (targetMonth > 12) {
            targetMonth = 1;
            targetYear++;
        }

        // Определяем количество дней в целевом месяце
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(targetYear, targetMonth - 1, 1); // Устанавливаем 1 число нужного месяца
        int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Если указанное число превышает число дней в месяце, ставим последний день
        int finalDay = Math.min(day, maxDaysInMonth);

        // Форматируем дату в строку "dd-MM-yyyy"
        String nextDate = String.format("%02d-%02d-%04d", finalDay, targetMonth, targetYear);

        // Записываем в базу
        contentValues2.put(COLUMN_NEXT, nextDate);

        long result = db.insert(TABLE_INCOME, null, contentValues2);

        if (result == -1) {
            System.out.println("Ошибка вставки в базу данных!");
        }

        return result != -1;
    }




    public int getIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_INCOME + ") FROM " + TABLE_INCOME, null);
        int income = 0;
        if (cursor.moveToFirst()) {
            income = cursor.getInt(0);
        }
        cursor.close();
        return income;
    }

    public Cursor getIncomeList(){
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tableName, null);
    }

    public Cursor getIncomeListForSQL() {
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase(); // Должен быть writable для обновления данных

        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE count > 0", null);

        return cursor;
    }
    public void resetIncome(){
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_COUNT, 0);
        db.update(tableName, contentValues, "count > 0", null);
    }

    public void resetMonthlySpents(){
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_COUNT, 0);
        db.update(tableName, contentValues, "count > 0", null);
    }


    public int checkMonthSpents() {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalSpent = 0;

        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SPENT + ") FROM " + TABLE_MONTHLY_SPENT, null);
        if (cursor.moveToFirst()) {
            totalSpent += cursor.getInt(0);
        }
        cursor.close();

        return totalSpent;
    }

    public void setMonthly(String name, boolean isMonthly){
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = TABLE_INCOME;

        boolean monthly = false;

        if (isMonthly){
            monthly = true;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_ONCEINCOME, monthly);
        db.update(tableName, contentValues, " name = ?", new String[]{name});

    }

    public Cursor getMonthly(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COLUMN_ONCEINCOME + " FROM " + TABLE_INCOME + " WHERE " + COLUMN_NAME + " = ?",
                new String[]{name}
        );
    }

    public void logAllIncomeData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INCOME, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                int onceIncome = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ONCEINCOME));
                Log.d("DB_CHECK", "Name: " + name + ", OnceIncome: " + onceIncome);
            } while (cursor.moveToNext());
        } else {
            Log.e("DB_ERROR", "TABLE_INCOME is empty!");
        }
        cursor.close();
    }

    public boolean updateData(String itemName, int incomeDay, String newName, int newIncome) {
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String oldName = itemName;

        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_INCOMEDAY, incomeDay);
        contentValues.put(COLUMN_INCOME, newIncome);

        // Получаем текущую дату
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH начинается с 0
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Определяем месяц для установки COLUMN_NEXT
        int targetMonth = (currentDay > incomeDay) ? (currentMonth + 1) : currentMonth;
        int targetYear = currentYear;

        // Если месяц стал 13 (т.е. следующий год), исправляем
        if (targetMonth > 12) {
            targetMonth = 1;
            targetYear++;
        }

        // Определяем количество дней в целевом месяце
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(targetYear, targetMonth - 1, 1);
        int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Если указанное число превышает число дней в месяце, ставим последний день
        int finalDay = Math.min(incomeDay, maxDaysInMonth);

        // Форматируем дату в строку "dd-MM-yyyy"
        String nextDate = String.format("%02d-%02d-%04d", finalDay, targetMonth, targetYear);

        // Добавляем новый COLUMN_NEXT в contentValues
        contentValues.put(COLUMN_NEXT, nextDate);

        // Обновляем запись, где имя совпадает
        int result = db.update(
                tableName,
                contentValues,
                COLUMN_NAME + " = ?",
                new String[]{oldName}
        );

        return result > 0; // Если обновлено хотя бы 1 строка, вернет true
    }


    public void deleteIncome(String name, int day){
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = "name = ? AND incomeday = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};
        // Выполняем удаление
        db.delete(tableName, whereClause, whereArgs);


    }

    public int controlBudget(int income , int spent){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        int budget = income - spent;
        contentValues2.put(COLUMN_BUDGET,budget);

        db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);

        return  budget;
    }

    public void setCurs(String i){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_CURS,i);

        db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
    }

    public String getCurs(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor a = db.rawQuery("SELECT " + COLUMN_CURS + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        String curs = "";
        if (a != null && a.moveToFirst()) {
            curs = a.getString(a.getColumnIndexOrThrow(COLUMN_CURS));
        }
        if (a != null) {
            a.close();
        }
        return curs;
    }

    public void addIncome(int income){
        SQLiteDatabase db = this.getWritableDatabase();

        // Получаем курсор с данными
        Cursor budgetData = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        // Проверяем, что курсор не пуст и переходим на первую строку
        if (budgetData != null && budgetData.moveToFirst()) {
            int budget = budgetData.getInt(budgetData.getColumnIndexOrThrow(COLUMN_BUDGET));
            int newBudget = income + budget;

            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(COLUMN_BUDGET, newBudget);

            db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
        } else {
            Log.e("DB_ERROR", "Нет данных для обновления бюджета");
        }

        if (budgetData != null) {
            budgetData.close();
        }

    }

    public void addSpent(int spent){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor budgetData = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        // Проверяем, что курсор не пуст и переходим на первую строку
        if (budgetData != null && budgetData.moveToFirst()) {
            int budget = budgetData.getInt(budgetData.getColumnIndexOrThrow(COLUMN_BUDGET));
            int newBudget = budget - spent;

            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(COLUMN_BUDGET, newBudget);

            db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
        } else {
            Log.e("DB_ERROR", "Нет данных для обновления бюджета");
        }

        if (budgetData != null) {
            budgetData.close();
        }

    }


    public void setIncomeGiven(String name , String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Получаем текущую дату из COLUMN_NEXT
        Cursor cursor = db.query(TABLE_INCOME, new String[]{COLUMN_NEXT,COLUMN_COUNT}, COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ? ", new String[]{name,date}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String currentDate = cursor.getString(0);
            cursor.close();

            // 1. Получаем текущее значение count
            Cursor cursorc = db.query(TABLE_INCOME, new String[]{COLUMN_COUNT}, COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ? ", new String[]{name,date}, null, null, null);

            int count = 0;
            if (cursorc.moveToFirst()) {
                count = cursorc.getInt(0);
                count += 1;
            }
            cursorc.close();

            // Разбираем текущую дату
            String[] parts = currentDate.split("-");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            // Увеличиваем месяц
            month += 1;
            if (month > 12) {
                month = 1;
                year++;
            }

            // Определяем последний день нового месяца
            Calendar targetDate = Calendar.getInstance();
            targetDate.set(year, month - 1, 1);
            int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Если указанное число превышает число дней в месяце, ставим последний день
            int finalDay = Math.min(day, maxDaysInMonth);

            // Формируем новую дату в формате "dd-MM-yyyy"
            String nextDate = String.format("%02d-%02d-%04d", finalDay, month, year);

            // Обновляем запись в базе
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NEXT, nextDate);
            contentValues.put(COLUMN_COUNT , count);

            db.update(TABLE_INCOME, contentValues, COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ? ", new String[]{name,date});
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    public void setLastActivity(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String date = sdf.format(new Date());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();

        contentValues2.put(COLUMN_LASTACTIVITY, date);
        db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
    }

    public String getLastActivity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_LASTACTIVITY + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        String lastActivity = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                lastActivity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LASTACTIVITY));
            }
            cursor.close();
        }
        return lastActivity;
    }


    public void testsetActivity(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();

        contentValues2.put(COLUMN_LASTACTIVITY, "0");
        db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
    }

    public int getBudget(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor a = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        int budget = 0; // Значение по умолчанию
        if (a != null && a.moveToFirst()) {
            budget = a.getInt(a.getColumnIndexOrThrow(COLUMN_BUDGET));
        }
        if (a != null) {
            a.close();
        }
        return budget;
    }

    public void monthchanged() {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteOnceIncome(db);
    }

    private boolean deleteOnceIncome(SQLiteDatabase db) {
        Cursor getheredData = db.rawQuery(
                "SELECT * FROM " + TABLE_INCOME +
                        " WHERE " + COLUMN_ONCEINCOME + " = ?",
                new String[]{String.valueOf("0")}
        );
        long result = 0;

        if (getheredData.moveToFirst()) {
            String name = getheredData.getString(getheredData.getColumnIndexOrThrow("name"));
            int spent = getheredData.getInt(getheredData.getColumnIndexOrThrow("income"));
            do{
                String whereClause = " name = ? AND income = ? ";
                String[] whereArgs = new String[]{name , String.valueOf(spent)};
                // Выполняем удаление
                db.delete(TABLE_INCOME, whereClause, whereArgs);
            } while (getheredData.moveToNext());
        }

        return result != 1;
    }

    public boolean addMonthlySpent(String name, int spent, int day) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_NAME, name);
        contentValues2.put(COLUMN_SPENT, spent);
        contentValues2.put(COLUMN_SPENTDAY, day);
        contentValues2.put(COLUMN_COUNT, 0);

        // Получаем текущую дату
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1; // Calendar.MONTH начинается с 0
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Определяем месяц для установки COLUMN_NEXT
        int targetMonth = (currentDay > day) ? (currentMonth + 1) : currentMonth;
        int targetYear = currentYear;

        // Если месяц стал 13 (т.е. следующий год), исправляем
        if (targetMonth > 12) {
            targetMonth = 1;
            targetYear++;
        }

        // Определяем количество дней в целевом месяце
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(targetYear, targetMonth - 1, 1); // Устанавливаем 1 число нужного месяца
        int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Если указанный день превышает количество дней в месяце, ставим последний день
        int finalDay = Math.min(day, maxDaysInMonth);

        // Форматируем дату в строку "dd-MM-yyyy"
        String nextDate = String.format("%02d-%02d-%04d", finalDay, targetMonth, targetYear);

        // Записываем в базу
        contentValues2.put(COLUMN_NEXT, nextDate);

        long result = db.insert(TABLE_MONTHLY_SPENT, null, contentValues2);

        if (result == -1) {
            System.out.println("Ошибка вставки в базу данных!");
        }

        return result != -1;
    }


    public boolean changeDate(String name, int date, int count , boolean isincome) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_INCOMEDAY, date);
        int result;
        if (isincome) {
            result = db.update(TABLE_INCOME, values, COLUMN_NAME + " = ? AND " + COLUMN_INCOME + " = ?", new String[]{name, String.valueOf(count)});
        } else {
            result = db.update(TABLE_MONTHLY_SPENT, values, COLUMN_NAME + " = ? AND " + COLUMN_SPENT + " = ?", new String[]{name, String.valueOf(count)});
        }
        return result > 0;
    }

    public void deleteMonthlySpent(String name , int day){
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = "name = ? AND " + COLUMN_SPENTDAY + " = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};
        // Выполняем удаление
        db.delete(tableName, whereClause, whereArgs);
    }

    public Cursor getMonthlySpentList(){
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tableName, null);
    }

    public Cursor getMonthlySpentListForSQL() {
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE count > 0", null);

        return cursor;
    }


    public void setMonthlySpentGiven(String name, String day) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_MONTHLY_SPENT, new String[]{COLUMN_NEXT, COLUMN_COUNT},
                COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{name, String.valueOf(day)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String currentDate = cursor.getString(0);
            int count = cursor.getInt(1);
            cursor.close();


            String[] parts = currentDate.split("-");
            int storedDay = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            month += 1;
            if (month > 12) {
                month = 1;
                year++;
            }

            Calendar targetDate = Calendar.getInstance();
            targetDate.set(year, month - 1, 1);
            int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

            int finalDay = Math.min(storedDay, maxDaysInMonth);

            String nextDate = String.format("%02d-%02d-%04d", finalDay, month, year);

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NEXT, nextDate);
            contentValues.put(COLUMN_COUNT, count + 1);

            db.update(TABLE_MONTHLY_SPENT, contentValues,
                    COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                    new String[]{name, String.valueOf(day)});
        }

        if (cursor != null) {
            cursor.close();
        }
    }




    public boolean updateMonthlySpent(String itemName, int spentDay, String newName, int newSpent) {
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String oldName = itemName;

        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_SPENTDAY, spentDay);
        contentValues.put(COLUMN_SPENT, newSpent);

        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        int targetMonth = (currentDay > spentDay) ? (currentMonth + 1) : currentMonth;
        int targetYear = currentYear;

        if (targetMonth > 12) {
            targetMonth = 1;
            targetYear++;
        }

        Calendar targetDate = Calendar.getInstance();
        targetDate.set(targetYear, targetMonth - 1, 1);
        int maxDaysInMonth = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        int finalDay = Math.min(spentDay, maxDaysInMonth);

        String nextDate = String.format("%02d-%02d-%04d", finalDay, targetMonth, targetYear);

        contentValues.put(COLUMN_NEXT, nextDate);

        int result = db.update(
                tableName,
                contentValues,
                "name = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{oldName, String.valueOf(spentDay)}
        );

        return result > 0;
    }


}

