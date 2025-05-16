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
    private static final String COLUMN_REPEAT = "repeat";
    private static final String COLUMN_DEFAULT = "defolt";

    private DatabaseHelper databaseHelper;

    public DatabaseHelper2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
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
                COLUMN_REPEAT + " INTEGER, " +
                COLUMN_SPENT + " REAL)";
        db.execSQL(createTableMonthlySpent);

        String createTableIncome = "CREATE TABLE IF NOT EXISTS " + TABLE_INCOME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_INCOME + " REAL, " +
                COLUMN_INCOMEDAY + " INTEGER, " +
                COLUMN_NEXT + " TEXT, " +
                COLUMN_COUNT + " INTEGER, " +
                COLUMN_REPEAT + " INTEGER, " +
                COLUMN_ONCEINCOME + " BOOLEAN)";
        db.execSQL(createTableIncome);

        String createTableBudget = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET + " REAL DEFAULT 0.0, " +
                COLUMN_DEFAULT + " TEXT, " +
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

    public boolean setIncome(double value, String name, int day, boolean once, int offset) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем предыдущую запись дохода с тем же именем
        db.delete(TABLE_INCOME, COLUMN_NAME + " = ?", new String[]{name});

        // Получаем сегодняшнюю дату и целевую дату дохода
        Calendar today = Calendar.getInstance();

        Calendar incomeDate = Calendar.getInstance();
        incomeDate.add(Calendar.MONTH, offset);
        int maxDay = incomeDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        incomeDate.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDay));

        // Формат даты
        String formattedDate = String.format("%02d-%02d-%04d",
                incomeDate.get(Calendar.DAY_OF_MONTH),
                incomeDate.get(Calendar.MONTH) + 1,
                incomeDate.get(Calendar.YEAR)
        );

        if (!incomeDate.after(today)) {
            // Если дата в прошлом или сегодня — просто вставляем как разовое начисление
            return databaseHelper.insertData(incomeDate.get(Calendar.DAY_OF_MONTH), name, -1 * value, 0, true);
        } else {
            // Если в будущем — сохраняем как планируемый доход
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_INCOME, value);
            contentValues.put(COLUMN_INCOMEDAY, day);
            contentValues.put(COLUMN_ONCEINCOME, once);
            contentValues.put(COLUMN_COUNT, 0);
            contentValues.put(COLUMN_NEXT, formattedDate);

            long result = db.insert(TABLE_INCOME, null, contentValues);
            return result != -1;
        }
    }



    public boolean setIncome(double value, String name, int day, boolean once, int repeat, int offset) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_INCOME, value);
        contentValues.put(COLUMN_INCOMEDAY, day);
        contentValues.put(COLUMN_ONCEINCOME, once);
        contentValues.put(COLUMN_REPEAT, repeat);
        contentValues.put(COLUMN_COUNT, 0);

        Calendar today = Calendar.getInstance();

        Calendar incomeDate = Calendar.getInstance();
        incomeDate.add(Calendar.MONTH, offset);
        incomeDate.set(Calendar.DAY_OF_MONTH, Math.min(day, incomeDate.getActualMaximum(Calendar.DAY_OF_MONTH)));

        if (repeat > 0) {
            while (!incomeDate.after(today)) {
                int incomeDay = incomeDate.get(Calendar.DAY_OF_MONTH);
                int incomeOffset = incomeDate.get(Calendar.MONTH) - today.get(Calendar.MONTH)
                        + 12 * (incomeDate.get(Calendar.YEAR) - today.get(Calendar.YEAR));

                databaseHelper.insertData(incomeDay, name, -1 * value, incomeOffset, true);
                addIncome(value);
                incomeDate.add(Calendar.DAY_OF_MONTH, repeat);
            }
        } else {
            while (!incomeDate.after(today)) {
                int incomeDay = incomeDate.get(Calendar.DAY_OF_MONTH);
                int incomeOffset = incomeDate.get(Calendar.MONTH) - today.get(Calendar.MONTH)
                        + 12 * (incomeDate.get(Calendar.YEAR) - today.get(Calendar.YEAR));

                databaseHelper.insertData(incomeDay, name, -1 * value, incomeOffset, true);
                addIncome(value);

                incomeDate.add(Calendar.MONTH, 1);
                int maxDayInMonth = incomeDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                incomeDate.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDayInMonth));
            }
        }

        // После всех начислений, сохраняем следующую дату
        String nextDate = String.format("%02d-%02d-%04d",
                incomeDate.get(Calendar.DAY_OF_MONTH),
                incomeDate.get(Calendar.MONTH) + 1,
                incomeDate.get(Calendar.YEAR)
        );

        contentValues.put(COLUMN_NEXT, nextDate);

        long result = db.insert(TABLE_INCOME, null, contentValues);
        return result != -1;
    }






    public double getIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_INCOME + ") FROM " + TABLE_INCOME, null);
        double income = 0;
        if (cursor.moveToFirst()) {
            income = cursor.getDouble(0);
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

    public String getDefaultCurrency(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor a = db.rawQuery("SELECT " + COLUMN_DEFAULT + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        String curs = "";
        if (a != null && a.moveToFirst()) {
            curs = a.getString(a.getColumnIndexOrThrow(COLUMN_DEFAULT));
        }
        if (a != null) {
            a.close();
        }
        return curs;
    }

    public void setDefaultCurrency(String currency){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_DEFAULT , currency);

        db.update(TABLE_BUDGET, contentValues2, COLUMN_ID + " = 1", null);
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

    public boolean updateData(int id,String newName, double newIncome) {
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_INCOME, newIncome);

        int result = db.update(
                tableName,
                contentValues,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0; // Если обновлено хотя бы 1 строка, вернет true
    }


    public void deleteIncome(int id) {
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();



        // Затем удаляем запись
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        db.delete(tableName, whereClause, whereArgs);

        db.close();
    }


    public void deleteAllOneTimeIncomes() {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ONCEINCOME + " = ? AND " + COLUMN_COUNT + " > ?";
        String[] selectionArgs = new String[]{"0", "0"};

        // Сначала получаем данные для логирования
        Cursor cursor = db.query(
                TABLE_INCOME,
                new String[]{COLUMN_NAME, COLUMN_COUNT, COLUMN_ONCEINCOME},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
                int onceIncome = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ONCEINCOME));

                Log.d("DELETE_LOG", "Удаляется: name = " + name + ", count = " + count + ", onceIncome = " + onceIncome);
            } while (cursor.moveToNext());

            cursor.close();
        }

        int deletedRows = db.delete(TABLE_INCOME, selection, selectionArgs);
        Log.d("DELETE_LOG", "Удалено строк: " + deletedRows);
    }




    public void deactivateIncome(String name, int day) {
        String tableName = TABLE_INCOME;
        SQLiteDatabase db = this.getWritableDatabase();

        // Дата "очень в будущем" в формате dd-MM-yyyy
        String nextDate = "01-01-3000";

        ContentValues values = new ContentValues();
        values.put("next", nextDate);

        String whereClause = "name = ? AND incomeday = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};

        db.update(tableName, values, whereClause, whereArgs);
    }

    public void deactivateSpent(String name, int day) {
        SQLiteDatabase db = this.getWritableDatabase();

        String nextDate = "01-01-3000";

        ContentValues values = new ContentValues();
        values.put(COLUMN_NEXT, nextDate);

        String whereClause = COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};

        db.update(TABLE_MONTHLY_SPENT, values, whereClause, whereArgs);
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

    public void addIncome(double income){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor budgetData = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        if (budgetData != null && budgetData.moveToFirst()) {
            double budget = budgetData.getDouble(budgetData.getColumnIndexOrThrow(COLUMN_BUDGET));
            double newBudget = income + budget;

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

    public void addSpent(double spent){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor budgetData = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        // Проверяем, что курсор не пуст и переходим на первую строку
        if (budgetData != null && budgetData.moveToFirst()) {
            double budget = budgetData.getDouble(budgetData.getColumnIndexOrThrow(COLUMN_BUDGET));
            double newBudget = budget - spent;

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

    public void setIncomeGivenByUser(String name, int date) {
        SQLiteDatabase db = this.getWritableDatabase();

        int count = 0;
        int repeat = -1;
        String currentDate = null;

        Cursor cursor = db.query(
                TABLE_INCOME,
                new String[]{COLUMN_NEXT, COLUMN_COUNT, COLUMN_REPEAT},
                COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ?",
                new String[]{name, String.valueOf(date)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_COUNT, count+1);

        db.update(TABLE_INCOME, contentValues, COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ?", new String[]{name, String.valueOf(date)});
        db.close();
    }


    public int setIncomeGiven(String name, int date) {
        SQLiteDatabase db = this.getWritableDatabase();

        int count = 0;
        int repeat = -1;
        String currentDate = null;

        Cursor cursor = db.query(
                TABLE_INCOME,
                new String[]{COLUMN_NEXT, COLUMN_COUNT, COLUMN_REPEAT},
                COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ?",
                new String[]{name, String.valueOf(date)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            currentDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEXT));
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
            repeat = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPEAT));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            return -1;
        }

        if (repeat == -1) {
            deactivateIncome(name, date);
            return 1;
        }

        // Сохраняем старое значение count
        int oldCount = count;

        // Разбор даты
        String[] parts = currentDate.split("-");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        Calendar targetDate = Calendar.getInstance();
        targetDate.set(year, month - 1, day);

        Calendar today = Calendar.getInstance();

        if (repeat == 0) {
            do {
                targetDate.add(Calendar.MONTH, 1);
                count++;

                int maxDay = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                targetDate.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDay));
            } while (!targetDate.after(today));
        } else {
            do {
                targetDate.add(Calendar.DAY_OF_YEAR, repeat);
                count++;
            } while (!targetDate.after(today));
        }

        String nextDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(targetDate.getTime());

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NEXT, nextDate);
        contentValues.put(COLUMN_COUNT, count);

        db.update(TABLE_INCOME, contentValues, COLUMN_NAME + " = ? AND " + COLUMN_INCOMEDAY + " = ?", new String[]{name, String.valueOf(date)});
        db.close();

        return count - oldCount; // возвращаем на сколько увеличился
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

    public double getBudget(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor a = db.rawQuery("SELECT " + COLUMN_BUDGET + " FROM " + TABLE_BUDGET + " WHERE " + COLUMN_ID + " = 1", null);

        double budget = 0; // Значение по умолчанию
        if (a != null && a.moveToFirst()) {
            budget = a.getDouble(a.getColumnIndexOrThrow(COLUMN_BUDGET));
        }
        if (a != null) {
            a.close();
        }
        return budget;
    }

    public void monthchanged() {
        SQLiteDatabase db = this.getWritableDatabase();
//        deleteOnceIncome(db);
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

    public boolean addMonthlySpent(String name, double spent, int day, int repeat, int category_id, int offset) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_SPENT, spent);
        contentValues.put(COLUMN_SPENTDAY, day);
        contentValues.put(COLUMN_CATEGORY, category_id);
        contentValues.put(COLUMN_COUNT, 0);
        contentValues.put(COLUMN_REPEAT, repeat);

        Calendar today = Calendar.getInstance();

        // Начальная дата траты с учётом смещения
        Calendar spentDate = Calendar.getInstance();
        spentDate.add(Calendar.MONTH, offset);
        spentDate.set(Calendar.DAY_OF_MONTH, Math.min(day, spentDate.getActualMaximum(Calendar.DAY_OF_MONTH)));

        if (repeat > 0) {
            while (!spentDate.after(today)) {
                int spentDay = spentDate.get(Calendar.DAY_OF_MONTH);
                int spentOffset = spentDate.get(Calendar.MONTH) - today.get(Calendar.MONTH)
                        + 12 * (spentDate.get(Calendar.YEAR) - today.get(Calendar.YEAR));

                databaseHelper.insertData(spentDay, name, spent, spentOffset, true, category_id);
                addSpent(spent);
                spentDate.add(Calendar.DAY_OF_MONTH, repeat);
            }
        } else {
            while (!spentDate.after(today)) {
                int spentDay = spentDate.get(Calendar.DAY_OF_MONTH);
                int spentOffset = spentDate.get(Calendar.MONTH) - today.get(Calendar.MONTH)
                        + 12 * (spentDate.get(Calendar.YEAR) - today.get(Calendar.YEAR));

                databaseHelper.insertData(spentDay, name, spent, spentOffset, true, category_id);
                addSpent(spent);

                spentDate.add(Calendar.MONTH, 1);
                int maxDayInMonth = spentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                spentDate.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDayInMonth));
            }
        }

        // Следующая дата траты
        String nextDate = String.format("%02d-%02d-%04d",
                spentDate.get(Calendar.DAY_OF_MONTH),
                spentDate.get(Calendar.MONTH) + 1,
                spentDate.get(Calendar.YEAR)
        );

        contentValues.put(COLUMN_NEXT, nextDate);

        long result = db.insert(TABLE_MONTHLY_SPENT, null, contentValues);
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

    public void deleteMonthlySpent(int id) {
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        db.delete(tableName, whereClause, whereArgs);
    }



    public Cursor getMonthlySpentList(){
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tableName, null);
    }

    public double getMonthlySpentSum() {
        double result = 0;
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SPENT + ") FROM " + tableName, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getDouble(0);
            }
            cursor.close();
        }

        return result;
    }


    public Cursor getMonthlySpentListForSQL() {
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE count > 0", null);

        return cursor;
    }

    public void setMonthlySpentGivenByUser(String name, int day) {
        SQLiteDatabase db = this.getWritableDatabase();

        int count = 0;


        Cursor cursor = db.query(
                TABLE_MONTHLY_SPENT,
                new String[]{COLUMN_NEXT, COLUMN_COUNT, COLUMN_REPEAT},
                COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{name, String.valueOf(day)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_COUNT, count+1);

        db.update(
                TABLE_MONTHLY_SPENT,
                contentValues,
                COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{name, String.valueOf(day)}
        );

        db.close();
    }

    public int setMonthlySpentGiven(String name, int day) {
        SQLiteDatabase db = this.getWritableDatabase();

        int count = 0;
        int repeat = -1;
        String currentDate = null;

        Cursor cursor = db.query(
                TABLE_MONTHLY_SPENT,
                new String[]{COLUMN_NEXT, COLUMN_COUNT, COLUMN_REPEAT},
                COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{name, String.valueOf(day)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            currentDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NEXT));
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
            repeat = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPEAT));
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            return -1;
        }

        if (repeat == -1) {
            deactivateSpent(name , day);
            return 1;
        }

        int oldCount = count;

        // Разбор текущей даты
        String[] parts = currentDate.split("-");
        int storedDay = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        Calendar targetDate = Calendar.getInstance();
        targetDate.set(year, month - 1, storedDay);

        Calendar today = Calendar.getInstance();

        if (repeat == 0) {
            do {
                targetDate.add(Calendar.MONTH, 1);
                count++;

                int maxDay = targetDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                targetDate.set(Calendar.DAY_OF_MONTH, Math.min(storedDay, maxDay));
            } while (!targetDate.after(today));
        } else {
            do {
                targetDate.add(Calendar.DAY_OF_YEAR, repeat);
                count++;
            } while (!targetDate.after(today));
        }

        // Форматирование следующей даты
        String nextDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(targetDate.getTime());

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NEXT, nextDate);
        contentValues.put(COLUMN_COUNT, count);

        db.update(
                TABLE_MONTHLY_SPENT,
                contentValues,
                COLUMN_NAME + " = ? AND " + COLUMN_SPENTDAY + " = ?",
                new String[]{name, String.valueOf(day)}
        );

        db.close();
        return count - oldCount;
    }





    public boolean updateMonthlySpent(int id, String newName, double newSpent, int category_id) {
        String tableName = TABLE_MONTHLY_SPENT;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_CATEGORY, category_id);
        contentValues.put(COLUMN_SPENT, newSpent);

        int result = db.update(
                tableName,
                contentValues,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }


    public int getCategoryId(int spentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int categoryId = -1; // значение по умолчанию, если не найдено

        Cursor cursor = db.rawQuery(
                "SELECT category_id FROM " + TABLE_MONTHLY_SPENT + " WHERE id = ?",
                new String[]{String.valueOf(spentId)}
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
            }
            cursor.close();
        }

        return categoryId;
    }

}

