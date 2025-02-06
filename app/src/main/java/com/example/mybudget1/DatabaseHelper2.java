package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper2 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "finance.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MONTHLY_SPENT = "monthly_spents";
    private static final String TABLE_INCOME = "income";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SPENT = "spent";
    private static final String COLUMN_INCOME = "income";

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
                COLUMN_INCOME + " INTEGER)";
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

    public boolean setIncome(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_INCOME, value);
        long result = db.insert(TABLE_INCOME, null, contentValues);
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
}

