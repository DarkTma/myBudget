package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {
    private LinearLayout menuLayout;
    private Button btnOpenMenu, btnCloseMenu, btnExpandList;
    private ListView listView;
    private WeekItemAdapter adapter;
    private List<String> dataList;
    private boolean isExpanded = false;
    private Button btnIncome;
    private TextView spentText;
    public Button monthlySpents;
    public Button lastMonths;
    public TextView budgetText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);


        // Инициализация элементов
        menuLayout = findViewById(R.id.menuLayout);
        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnCloseMenu = findViewById(R.id.btnCloseMenu);
        btnExpandList = findViewById(R.id.btnExpandList);
        btnIncome = findViewById(R.id.btnIncomeList);
        spentText = findViewById(R.id.tvSpent);
        listView = findViewById(R.id.listView);
        monthlySpents = findViewById(R.id.btnMonthlySpents);
        budgetText = findViewById(R.id.tvBudget);
        lastMonths = findViewById(R.id.btnLastMonths);
        TextView podskazka = findViewById(R.id.textpodskazka);


        //закрытие менюшки
        LinearLayout menuLayout = findViewById(R.id.menuLayout);
        View dimLayer = findViewById(R.id.dimLayer);

        lastMonths.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MonthListActivity.class);
            startActivity(intent);
            finish();
        });

        monthlySpents.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, SpentActivity.class);
            startActivity(intent);
            finish();
        });

        dimLayer.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        Button btnIncomeActivityGo = findViewById(R.id.btnincomeData);
        btnIncomeActivityGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, IncomeActivity.class);
            startActivity(intent);
            finish();
        });

        Button btnCategoriesGo = findViewById(R.id.btnCategories);
        btnCategoriesGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, CategoriesActivity.class);
            startActivity(intent);
            finish();
        });

        checkMonth();


        int spent = databaseHelper.checkAllSpents(0);
        spentText.setText("расход: " + spent + "₽");

        //int budget = databaseIncome.controlBudget(income , spent);
        refreshBudgetText();
        refreshIncomesDatas();

        btnIncome.setOnClickListener(v -> showincomeList());



        // Данные для ListView
        List<WeekItem> dataList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int getCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentDayIndex = getCurrentDay;
        int prevDay = currentDayIndex - 1;
        int nextDay = currentDayIndex + 1;

        String prevDayName = DayAdapter.getDayName(prevDay);
        String currentDayName = DayAdapter.getDayName(currentDayIndex);
        String nextDayName = DayAdapter.getDayName(nextDay);


        int prevDaySpent = databaseHelper.getDoneSpents(prevDay, prevDay);
        int prevDayMustDo = databaseHelper.getAllSpents(prevDay, prevDay);
        int todaySpent = databaseHelper.getDoneSpents(currentDayIndex, currentDayIndex);
        int todayMustDo = databaseHelper.getAllSpents(currentDayIndex, currentDayIndex);
        int nextDaySpent = databaseHelper.getDoneSpents(nextDay, nextDay);
        int nextDayMustDo = databaseHelper.getAllSpents(nextDay, nextDay);



        dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "из: " + prevDayMustDo + "₽"));
        dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "из: " + todayMustDo + "₽"));
        dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "из: " + nextDayMustDo + "₽"));


        adapter = new WeekItemAdapter(this, dataList);
        listView.setAdapter(adapter);

        // Открыть меню
        btnOpenMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.VISIBLE);
            dimLayer.setVisibility(View.VISIBLE);
        });

        // Закрыть меню
        btnCloseMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        // Расширение списка
        btnExpandList.setOnClickListener(v -> {
            if (isExpanded) {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                dataList.add(new WeekItem(prevDayName , "потрачено: " + prevDaySpent + "₽", "из: " + prevDayMustDo + "₽"));
                dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + todaySpent + "₽", "из: " + todayMustDo + "₽"));
                dataList.add(new WeekItem(nextDayName, "потрачено: " + nextDaySpent + "₽", "из: " + nextDayMustDo + "₽"));

                isExpanded = false;
                podskazka.setVisibility(View.GONE);
                listView.getLayoutParams().height -= 700; // Увеличиваем высоту
            } else {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                int day = DayAdapter.getStartOfWeek();
                String[] weekDays = {"понедельник", "вторник", "среда", "четверг" , "пятница", "суббота", "воскресение"};
                for (int i = 0; i < 7; i++) {
                    dataList.add(new WeekItem(weekDays[i] , "потрачено: " + databaseHelper.getDoneSpents(day+i, day+i)+"₽",
                            "из: " + databaseHelper.getAllSpents(day+i, day+i) + "₽"));
                }
                listView.getLayoutParams().height += 700; // Возвращаем высоту
                isExpanded = true;
                podskazka.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный элемент списка
                WeekItem selectedItem = (WeekItem) parent.getItemAtPosition(position);

                // Создаем Intent для перехода на новую активность
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                if (!isExpanded) {
//                    int moneday = DayAdapter.getStartOfWeek();
//                    int choosenDay = DayAdapter.findDayOfMonth(moneday, selectedItem.getDayName());
                    Calendar calendar = Calendar.getInstance();
                    int getCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int currentDayIndex = getCurrentDay;
                    int choosenDay = currentDayIndex;
                    if (position == 0){
                        choosenDay -= 1;
                    }else if(position == 2){
                        choosenDay += 1;
                    }
                    intent.putExtra("day", choosenDay);
                    intent.putExtra("isexpented", "false");
                }else {
                    intent.putExtra("day", position);
                    intent.putExtra("isexpented", "true");
                }

                // Запускаем новую активность
                startActivity(intent);
                finish();
            }
        });
    }


    //если пришло время дабавляем сумму дохода
    //исправить , бюджет щхитает не правильно
    private void refreshBudget() {
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Cursor income = databaseIncome.getIncomeList();
        if (income != null && income.moveToFirst()) {
            do {
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                String timeToGive = income.getString(income.getColumnIndexOrThrow("next"));
                String once = income.getString(income.getColumnIndexOrThrow("onceincome"));
                int day = income.getInt(income.getColumnIndexOrThrow("incomeday"));
                int incomeNum = income.getInt(income.getColumnIndexOrThrow("income"));

                boolean checkOnce = false;
                if (once.equals("0")) checkOnce = true;

                // Сравниваем текущую дату с timeToGive
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Формат даты
                    Date currentDate = new Date(); // Текущая дата
                    Date givenDate = sdf.parse(timeToGive); // Преобразуем timeToGive в объект Date

                    if (givenDate != null && !currentDate.before(givenDate)) {
                        checkGoten(name , day , incomeNum , true);
//                        databaseIncome.setIncomeGiven(name , day);
//                        databaseIncome.addIncome(incomeNum);
                    }

                    if (checkOnce) {
                        // Если checkOnce == true, вызываем функцию deleteIncome
                        databaseIncome.deleteIncome(name , day); // Вызов функции deleteIncome
                    }

                } catch (Exception e) {
                    e.printStackTrace(); // Обрабатываем исключения (например, если формат даты неверный)
                }

            } while (income.moveToNext());
        }


        Cursor spents = databaseIncome.getMonthlySpentList();
        if (spents != null && spents.moveToFirst()) {
            do {
                String name = spents.getString(spents.getColumnIndexOrThrow("name"));
                int spentNum = spents.getInt(spents.getColumnIndexOrThrow("monthly_spent"));
                int day = spents.getInt(spents.getColumnIndexOrThrow("spentday"));
                String timeToGive = spents.getString(spents.getColumnIndexOrThrow("next"));

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Формат даты
                    Date currentDate = new Date();
                    Date givenDate = sdf.parse(timeToGive);

                    if (givenDate != null && !currentDate.before(givenDate)) {
                        checkGoten(name , day , spentNum , false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (spents.moveToNext());
        }
    }




    @SuppressLint("SetTextI18n")
    private void refreshBudgetText() {
        DatabaseHelper2 databaseHelper2 = new DatabaseHelper2(this);
        int budget = databaseHelper2.getBudget();
        budgetText.setText("бюджет: " + String.valueOf(budget) + "₽");

    }

    private void showincomeList() {
        ArrayList<String> dataList = getIncomData();

        // Кастомный адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.income_item_forshow, R.id.tvItem, dataList);

        // Создаем ListView
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        // Создаем AlertDialog с кастомным фоном
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#1EFF00'>Ваши доходы (чтоб изменить их зайдите в меню)</font>"));
        builder.setView(listView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Создаем AlertDialog
        AlertDialog dialog = builder.create();

        // Устанавливаем фон из drawable
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        // Показываем диалог
        dialog.show();
    }

    private ArrayList<String> getIncomData(){
        ArrayList<String> dataList = new ArrayList<>();
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Cursor cursor = databaseIncome.getIncomeList();
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                int spent = cursor.getInt(2);
                int day = cursor.getInt(3);
                int once = cursor.getInt(4);
                String isOnce = "одноразовый";
                if (once == 1){
                    isOnce = "ежемесечный";
                }
                dataList.add("\n" + "данные дохода: " + name + " - " + spent + "₽" + "\n" + "день получения: " + day + " числа"  + "\n" + "тип: " + isOnce);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }


    private void refreshIncomesDatas(){
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String lastDate = databaseIncome.getLastActivity();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        String today = sdf.format(new Date());

        boolean newDay = true;

        try {
            Date d1 = sdf.parse(today);
            Date d2 = sdf.parse(lastDate);
            newDay = d2.before(d1); // Возвращает true, если date1 раньше date2
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (lastDate.equals("0")){
            databaseIncome.setLastActivity();
        } else if(newDay){
            databaseIncome.setLastActivity();
            refreshBudget();
            remembring(today);
        }
    }

    private void checkGoten(String name, int date , int count , boolean type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (type) { // Если это доход
            builder.setMessage("Вы получили зарплату - " + name + ", " + count + " ?");

            builder.setPositiveButton("Да", (dialog, which) -> {
                databaseIncome.addIncome(count);
                databaseIncome.setIncomeGiven(name , String.valueOf(date));
                refreshBudgetText();
            });

            builder.setNeutralButton("Отложить", (dialog, which) -> dialog.dismiss());

            builder.setNegativeButton("Удалить", (dialog, which) -> databaseIncome.deleteIncome(name, date));

        } else {
            builder.setMessage("Вы выполнили трату - " + name + ", " + count + " ?");

            builder.setPositiveButton("Да", (dialog, which) -> {
                databaseIncome.addSpent(count);
                databaseIncome.setMonthlySpentGiven(name , String.valueOf(date));
                refreshBudgetText();
            });

            builder.setNeutralButton("Отложить", (dialog, which) -> dialog.dismiss());

            builder.setNegativeButton("Удалить", (dialog, which) -> databaseIncome.deleteMonthlySpent(name, date));
        }

        AlertDialog dialog = builder.create();

        // Применяем фон
        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

            // Применяем цвет кнопкам
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.my_cyan));
        });

        dialog.show();
    }


    private void remembring(String date) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        int today = Integer.parseInt(date.split("-")[0]);
        ArrayList<String> dataList2 = new ArrayList<>();
        boolean show = false;
        String name;
        int spent;
        int day;
        if (today > 1) {
            Cursor allNoteDoneSpents = databaseHelper.getNotDoneSpentsOfMonth();
            if (allNoteDoneSpents != null && allNoteDoneSpents.moveToFirst()) {
                do {
                    show = true;
                    name = allNoteDoneSpents.getString(allNoteDoneSpents.getColumnIndexOrThrow("name"));
                    spent = allNoteDoneSpents.getInt(allNoteDoneSpents.getColumnIndexOrThrow("spent"));
                    day = allNoteDoneSpents.getInt(allNoteDoneSpents.getColumnIndexOrThrow("day"));
                    if(today > day) {
                        dataList2.add(name + " - " + spent + "₽ , " + day + " числа\n");
                    }
                } while (allNoteDoneSpents.moveToNext());
            }
        }

        if (show){


            // Кастомный адаптер
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.income_item_forshow, R.id.tvItem, dataList2);

            // Создаем ListView
            ListView listView = new ListView(this);
            listView.setAdapter(adapter);

            // Создаем AlertDialog с кастомным фоном
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(Html.fromHtml("<font color='#1EFF00'>вчера у вас осталось не невыполненые траты</font>"));
            builder.setView(listView);
            builder.setPositiveButton("я выполнил их", (dialog, which) -> {
                    DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
                        String itemName = "";
                        int itemSpent = 0;
                        int itemDay = 1;
                        Cursor allSpents = databaseHelper.getNotDoneSpentsOfMonth();
                        if (allSpents != null && allSpents.moveToFirst()) {
                            do {
                                itemName = allSpents.getString(allSpents.getColumnIndexOrThrow("name"));
                                itemSpent = allSpents.getInt(allSpents.getColumnIndexOrThrow("spent"));
                                itemDay = allSpents.getInt(allSpents.getColumnIndexOrThrow("day"));
                                if(today > itemDay) {
                                    databaseHelper.setDone(itemName, itemDay, 0, true);
                                    databaseIncome.addSpent(itemSpent);
                                }
                            } while (allSpents.moveToNext());
                        }
                    dialog.dismiss();
            })
                    .setNegativeButton("закрыть", null);

            // Создаем AlertDialog
            AlertDialog dialog = builder.create();

            // Устанавливаем фон из drawable
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

            // Показываем диалог
            dialog.show();
        }
    }

    private void checkMonth(){
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        String oldData = databaseIncome.getLastActivity();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String today = sdf.format(new Date());

        //проверяем изменился ли месяц
        if (!today.split("-")[1].equals(oldData.split("-")[0]) && !oldData.equals("")){
            monthChanged();
        }else {
            System.out.println("dsgfdg");
        }
    }

    private void monthChanged() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        databaseHelper.monthchanged();
        // Получаем данные из базы
        Cursor incomeCursor = databaseIncome.getIncomeListForSQL();
        Cursor spentCursor = databaseIncome.getMonthlySpentListForSQL();

        // Сохраняем данные в prevMonth
        databaseHelper.saveIncomeToPrevMonth(incomeCursor);
        databaseHelper.saveSpentToPrevMonth(spentCursor);

        databaseIncome.resetIncome();
        databaseIncome.resetMonthlySpents();

        databaseIncome.monthchanged();
    }
}

