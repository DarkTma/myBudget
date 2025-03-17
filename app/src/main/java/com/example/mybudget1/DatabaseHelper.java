package com.example.mybudget1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;

    // Названия таблиц для хранения данных трех месяцев
    private String prevMonthTable;
    private String currentMonthTable;
    private String nextMonthTable;


    // Колонки таблицы
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category_id";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DONE = "isdone";
    private static final String COLUMN_SPENT = "spent";

    private List<String> categories;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        updateMonthTables();
//        categories = readCategoriesFromFile(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, prevMonthTable);
        createTable(db, currentMonthTable);
        createTable(db, nextMonthTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + prevMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + currentMonthTable);
        db.execSQL("DROP TABLE IF EXISTS " + nextMonthTable);
        onCreate(db);
    }

    private void updateMonthTables(SQLiteDatabase db) {
        db.beginTransaction(); // Начинаем транзакцию
        try {
            // Загружаем список существующих таблиц (от нового к старому)
            List<String> monthTables = getMonthTables();

            // Генерируем имя для нового месяца на основе первого элемента списка
            String newMonth = generateNextMonthName(monthTables.get(0));

            // Добавляем новый месяц в начало списка
            monthTables.add(0, newMonth);

            // Создаем новую таблицу
            createTable(db, newMonth);
            Log.d("SQLite", "Создана новая таблица: " + newMonth);

            // Определяем переменные next, current, prev по индексам списка
            if (monthTables.size() > 2) {
                nextMonthTable = monthTables.get(0);
                currentMonthTable = monthTables.get(1);
                prevMonthTable = monthTables.get(2);

                // Создаем таблицы для доходов и расходов в prevMonth
                createIncomeTable(db, prevMonthTable);
                createSpentTable(db, prevMonthTable);

                Log.d("SQLite", "Next: " + nextMonthTable);
                Log.d("SQLite", "Current: " + currentMonthTable);
                Log.d("SQLite", "Prev: " + prevMonthTable);
            }

            db.setTransactionSuccessful(); // Завершаем транзакцию
        } catch (Exception e) {
            Log.e("SQLite", "Ошибка при обновлении таблиц: " + e.getMessage());
        } finally {
            db.endTransaction(); // Закрываем транзакцию
        }
    }

//    public List<String> readCategoriesFromFile(Context context) {
//        List<String> categories = new ArrayList<>();
//        try {
//            // Попытка открыть файл для чтения
//            FileInputStream fis = context.openFileInput("categories.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
//            String line;
//
//            // Чтение всех категорий из файла
//            while ((line = reader.readLine()) != null) {
//                String[] categoryArray = line.split("-");
//                for (String category : categoryArray) {
//                    if (!category.isEmpty()) {
//                        categories.add(category);  // Добавляем категорию, если она не пустая
//                    }
//                }
//            }
//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            createDefaultCategoriesFile(context);
//        }
//        return categories;
//    }

    private void createDefaultCategoriesFile(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("categories.txt", Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write("other");  // Записываем дефолтную категорию в файл
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MonthData> getMonthData(SQLiteDatabase db) {
        List<MonthData> monthDataList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'month_%' ORDER BY name DESC", null);

        while (cursor.moveToNext()) {
            String tableName = cursor.getString(0);  // Имя таблицы, например, month_2025_3

            // Проверяем, существует ли таблица для доходов (income) и расходов (spent)
            if (tableExists(db, tableName + "_income") && tableExists(db, tableName + "_spent")) {
                // Получаем информацию о доходах и расходах
                double totalIncome = getTotalIncomeForMonth(db, tableName);
                double totalSpent = getTotalSpentForMonth(db, tableName);

                // Создаем объект MonthData и добавляем его в список
                monthDataList.add(new MonthData(tableName, (int) totalIncome, (int) totalSpent));
            }
        }

        cursor.close();
        return monthDataList;
    }

    // Метод для получения общего дохода для месяца
    private double getTotalIncomeForMonth(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT SUM(income) FROM " + tableName + "_income", null);
        cursor.moveToFirst();
        double totalIncome = cursor.getDouble(0);
        cursor.close();
        return totalIncome;
    }

    // Метод для получения общих расходов для месяца
    private double getTotalSpentForMonth(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT SUM(spent) FROM " + tableName + "_spent", null);
        cursor.moveToFirst();
        double totalSpent = cursor.getDouble(0);
        cursor.close();
        return totalSpent;
    }

    // Метод для проверки существования таблицы
    public boolean tableExists(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<String> getMonthTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> tables = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'month_%' ORDER BY name DESC", null);

        while (cursor.moveToNext()) {
            String tableName = cursor.getString(0);  // Имя таблицы, например, month_2025_3

            // Проверяем, что имя таблицы соответствует шаблону 'month_год_месяц'
            if (tableName.matches("month_\\d{4}_\\d{1,2}")) {
                tables.add(tableName);  // Добавляем в список, если имя таблицы соответствует шаблону
            }
        }

        cursor.close();
        return tables;
    }



    // Метод для проверки существования таблицы
    public boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private double getIncomeForMonth(String monthTableName) {
        SQLiteDatabase database = this.getReadableDatabase();
        String incomeTable = monthTableName + "_income";

        // Проверяем, существует ли таблица для доходов
        if (!tableExists(incomeTable)) {
            return 0.0;  // Если таблица не существует, возвращаем 0
        }

        // Если таблица существует, выполняем запрос
        Cursor cursor = database.rawQuery("SELECT SUM(income) FROM " + incomeTable, null);
        if (cursor.moveToFirst()) {
            double income = cursor.getDouble(0);
            cursor.close();
            return income;
        }
        cursor.close();
        return 0.0;
    }

    private double getSpentForMonth(String monthTableName) {
        SQLiteDatabase database = this.getReadableDatabase();
        String spentTable = monthTableName + "_spent";

        // Проверяем, существует ли таблица для расходов
        if (!tableExists(spentTable)) {
            return 0.0;  // Если таблица не существует, возвращаем 0
        }

        // Если таблица существует, выполняем запрос
        Cursor cursor = database.rawQuery("SELECT SUM(spent) FROM " + spentTable, null);
        if (cursor.moveToFirst()) {
            double spent = cursor.getDouble(0);
            cursor.close();
            return spent;
        }
        cursor.close();
        return 0.0;
    }

    // Метод для проверки существования таблицы


    // Генерирует имя следующего месяца на основе последней таблицы
    private String generateNextMonthName(String lastMonth) {
        Pattern pattern = Pattern.compile("month_(\\d+)_(\\d+)");
        Matcher matcher = pattern.matcher(lastMonth);

        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));

            month++; // Увеличиваем месяц
            if (month > 12) {
                month = 1;
                year++;
            }

            return String.format("month_%d_%d", year, month);
        }

        return "month_unknown";
    }


    private void createTable(SQLiteDatabase db, String tableName) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY + " INTEGER DEFAULT 0, " +
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
    public boolean insertData(int day, String name, int spent, int offset , boolean isDone , int category) {
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
                    contentValues.put(COLUMN_CATEGORY, category);
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

    public void deleteData(String name, int day , int offset){
        String tableName;
        if (offset == -1) {
            tableName = prevMonthTable;
        } else if (offset == 0) {
            tableName = currentMonthTable;
        } else {
            tableName = nextMonthTable;
        }
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = "name = ? AND day = ?";
        String[] whereArgs = new String[]{name, String.valueOf(day)};
        // Выполняем удаление
        db.delete(tableName, whereClause, whereArgs);


    }

    public boolean updateData(String itemName, int currentDay, String newName, int newSpent , int offset) {
        String tableName;
        if (offset == -1) {
            tableName = prevMonthTable;
        } else if (offset == 0) {
            tableName = currentMonthTable;
        } else {
            tableName = nextMonthTable;
        }
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


    public int checkAllSpents(int offset){
        String tableNameforDay = currentMonthTable;
        if (offset == -1) {
            tableNameforDay = prevMonthTable;
        }
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


    // prev month edits
    // Создание таблицы для доходов в prevMonth
    private void createIncomeTable(SQLiteDatabase db, String prevMonthTable) {
        String createIncomeTableQuery = "CREATE TABLE IF NOT EXISTS " + prevMonthTable + "_income (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "income INTEGER, " +
                "day INTEGER)";
        db.execSQL(createIncomeTableQuery);
    }

    // Создание таблицы для расходов в prevMonth
    private void createSpentTable(SQLiteDatabase db, String prevMonthTable) {
        String createSpentTableQuery = "CREATE TABLE IF NOT EXISTS " + prevMonthTable + "_spent (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "spent INTEGER, " +
                "day INTEGER)";
        db.execSQL(createSpentTableQuery);
    }

    // Сохранение доходов в таблицу prevMonth_income
    // Сохранение доходов в таблицу prevMonth_income
    public void saveIncomeToPrevMonth(Cursor cursor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int income = cursor.getInt(cursor.getColumnIndexOrThrow("income"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("incomeday"));

                // Вставляем данные в таблицу prevMonth_income
                String insertIncomeQuery = "INSERT INTO " + prevMonthTable + "_income (name, income, day) VALUES (?, ?, ?)";
                db.execSQL(insertIncomeQuery, new Object[]{name, income, day});

            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // Сохранение расходов в таблицу prevMonth_spent
    public void saveSpentToPrevMonth(Cursor cursor) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int spent = cursor.getInt(cursor.getColumnIndexOrThrow("spent"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("spentday"));

                // Вставляем данные в таблицу prevMonth_spent
                String insertSpentQuery = "INSERT INTO " + prevMonthTable + "_spent (name, spent, day) VALUES (?, ?, ?)";
                db.execSQL(insertSpentQuery, new Object[]{name, spent, day});

            } while (cursor.moveToNext());
        }
        insertSpentIfDone();
        if (cursor != null) {
            cursor.close();
        }
    }

    public void insertSpentIfDone() {
        String tableName = prevMonthTable;

        SQLiteDatabase db = this.getWritableDatabase();

        // Строим запрос для выборки данных с DONE = true
        String selectQuery = "SELECT " + COLUMN_NAME + ", " + COLUMN_DAY + ", " + COLUMN_SPENT + " FROM " + tableName +
                " WHERE " + COLUMN_DONE + " = 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            // Итерируем по всем записям с DONE = true
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int spent = cursor.getInt(cursor.getColumnIndexOrThrow("spent"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));

                // Записываем эти данные в таблицу month_xxxx_x_spent
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, name);
                values.put(COLUMN_DAY, day);
                values.put(COLUMN_SPENT, spent);

                // Вставляем данные в таблицу month_xxxx_x_spent
                db.insert(tableName + "_spent", null, values);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }


//    public List<MonthDetailData> getMonthDetailData(SQLiteDatabase db, String MonthTable) {
//        List<MonthDetailData> monthDetailDataList = new ArrayList<>();
//
//        // Извлекаем доходы для выбранного месяца
//        String incomeQuery = "SELECT * FROM " + MonthTable + "_income";
//        Cursor incomeCursor = db.rawQuery(incomeQuery, null);
//
//        while (incomeCursor.moveToNext()) {
//            String name = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("name"));
//            int income = incomeCursor.getInt(incomeCursor.getColumnIndexOrThrow("income"));
//            int day = incomeCursor.getInt(incomeCursor.getColumnIndexOrThrow("day"));
//            monthDetailDataList.add(new MonthDetailData("Income", name, income, day));
//        }
//        incomeCursor.close();
//
//        // Извлекаем расходы для выбранного месяца
//        String spentQuery = "SELECT * FROM " + MonthTable + "_spent";
//        Cursor spentCursor = db.rawQuery(spentQuery, null);
//
//        while (spentCursor.moveToNext()) {
//            String name = spentCursor.getString(spentCursor.getColumnIndexOrThrow("name"));
//            int spent = spentCursor.getInt(spentCursor.getColumnIndexOrThrow("spent"));
//            int day = spentCursor.getInt(spentCursor.getColumnIndexOrThrow("day"));
//            monthDetailDataList.add(new MonthDetailData("Spent", name, spent, day));
//        }
//        spentCursor.close();
//
//        return monthDetailDataList;
//    }

    public List<MonthDetailData> getMonthDetailData(SQLiteDatabase db, String monthTable) {
        List<MonthDetailData> detailList = new ArrayList<>();

        // Запрос для доходов
        String incomeQuery = "SELECT name, income, day FROM " + monthTable + "_income ORDER BY day ASC";
        Cursor cursor = db.rawQuery(incomeQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow("income"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                detailList.add(new MonthDetailData("Income", name, amount, day));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Запрос для расходов
        String spentQuery = "SELECT name, spent, day FROM " + monthTable + "_spent ORDER BY day ASC";
        cursor = db.rawQuery(spentQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow("spent"));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow("day"));
                detailList.add(new MonthDetailData("Spent", name, amount, day));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Сортируем список по дню
        Collections.sort(detailList, (d1, d2) -> Integer.compare(d1.getDay(), d2.getDay()));

        return detailList;
    }




}


