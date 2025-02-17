package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper2 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finance.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MONTHLY_SPENT = "monthly_spents";
    private static final String TABLE_INCOME = "income";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SPENT = "spent";
    private static final String COLUMN_INCOME = "income";
    private static final String COLUMN_INCOMEDAY = "incomeday";
    private static final String COLUMN_ONCEINCOME = "onceincome";

    public DatabaseHelper2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableMonthlySpent = "CREATE TABLE IF NOT EXISTS " + TABLE_MONTHLY_SPENT + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SPENT + " INTEGER)";
        db.execSQL(createTableMonthlySpent);

        String createTableIncome = "CREATE TABLE IF NOT EXISTS " + TABLE_INCOME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_INCOME + " INTEGER, " +
                COLUMN_INCOMEDAY + " INTEGER, " +
                COLUMN_ONCEINCOME + " BOOLEAN)";
        db.execSQL(createTableIncome);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTHLY_SPENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
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

    public boolean setIncome(int value , String name , int day , boolean once) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COLUMN_NAME, name);
        contentValues2.put(COLUMN_INCOME, value);
        contentValues2.put(COLUMN_INCOMEDAY, day);
        contentValues2.put(COLUMN_ONCEINCOME, once); // Приведение boolean к int

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

        // Обновляем запись, где день и старое имя совпадают
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

        String whereClause = "name = ? AND day = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};
        // Выполняем удаление
        db.delete(tableName, whereClause, whereArgs);


    }

}

