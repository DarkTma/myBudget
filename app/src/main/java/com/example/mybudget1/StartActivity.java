package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {
    private LinearLayout menuLayout;
    private Button btnOpenMenu, btnExpandList;
    private ListView listView;
    private WeekItemAdapter adapter;
    private List<String> dataList;
    private boolean isExpanded = false;
    private TextView spentText;
    public Button monthlySpents;
    public Button lastMonths;
    public Button geminiAnalizbtn;
    public TextView budgetText;
    public CursData curs;




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
        btnExpandList = findViewById(R.id.btnExpandList);
        spentText = findViewById(R.id.tvSpent);
        listView = findViewById(R.id.listView);
        monthlySpents = findViewById(R.id.btnMonthlySpents);
        budgetText = findViewById(R.id.tvBudget);
        lastMonths = findViewById(R.id.btnLastMonths);
        TextView podskazka = findViewById(R.id.textpodskazka);
        ImageButton btnScan = findViewById(R.id.buttonScan);
        geminiAnalizbtn = findViewById(R.id.btnGeminiGo);

        geminiAnalizbtn.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, GeminiChatActivity.class);
            startActivity(intent);
            finish();
        });

        Button btnConf = findViewById(R.id.btnConf);
        btnConf.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            finish();
        });


        curs = CursHelper.getCursData(databaseIncome.getCurs());

        Intent intenterr = getIntent();
        String err = intenterr.getStringExtra("error");
        if (err != null){
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Log.d("CurrencyDebug", "Saving currency: " + databaseIncome.getDefaultCurrency());
                finishAffinity();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        deleteOldNotifications();
        showNotifCount();

        Button btnGoalGo = findViewById(R.id.btnGoalGo);
        btnGoalGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, GoalActivity.class);
            startActivity(intent);
            finish();
        });

        ImageButton btnNotif = findViewById(R.id.notifications);
        btnNotif.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, ReminderListActivity.class);
            startActivity(intent);
            finish();
        });


        ImageButton buttonHistory = findViewById(R.id.buttonHistory);
        buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, NotesActivity.class);
            startActivity(intent);
            finish();
        });

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

        Button btnGrafGo = findViewById(R.id.btnGrafGo);
        btnGrafGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, GraphActivity.class);
            startActivity(intent);
            finish();
        });

        Button btnMaketsGo = findViewById(R.id.btnMaketGo);
        btnMaketsGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MaketListActivity.class);
            startActivity(intent);
            finish();
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

        Button btnCursGo = findViewById(R.id.btnCurs);
        btnCursGo.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, CurrencyActivity.class);
            startActivity(intent);
            finish();
        });

        checkMonth();


        double spent = databaseHelper.checkAllSpents(0);
        String result = String.format("%.2f %s", spent * curs.rate , curs.symbol);
        spentText.setText("расход: " + result);

        //int budget = databaseIncome.controlBudget(income , spent);
        refreshBudgetText();
        refreshIncomesDatas();


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



        double prevDaySpent = databaseHelper.getDoneSpents(prevDay, prevDay);
        double prevDayMustDo = databaseHelper.getAllSpents(prevDay, prevDay);
        double todaySpent = databaseHelper.getDoneSpents(currentDayIndex, currentDayIndex);
        double todayMustDo = databaseHelper.getAllSpents(currentDayIndex, currentDayIndex);
        double nextDaySpent = databaseHelper.getDoneSpents(nextDay, nextDay);
        double nextDayMustDo = databaseHelper.getAllSpents(nextDay, nextDay);



        adapter = new WeekItemAdapter(this, dataList);
        listView.setAdapter(adapter);

        // Открыть меню
        btnOpenMenu.setOnClickListener(v -> {
            menuLayout.setVisibility(View.VISIBLE);
            dimLayer.setVisibility(View.VISIBLE);
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, ScanReceiptActivity.class);
            startActivity(intent);
            finish();
        });

        DecimalFormat df = new DecimalFormat("0.##");

        dataList.add(new WeekItem(prevDayName , "потрачено: " + df.format(prevDaySpent  * curs.rate) + curs.symbol, "из: " + df.format(prevDayMustDo  * curs.rate) + curs.symbol));
        dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + df.format(todaySpent * curs.rate) + curs.symbol, "из: " + df.format(todayMustDo  * curs.rate) + curs.symbol));
        dataList.add(new WeekItem(nextDayName, "потрачено: " + df.format(nextDaySpent * curs.rate) + curs.symbol, "из: " + df.format(nextDayMustDo * curs.rate) + curs.symbol));

        // Расширение списка
        btnExpandList.setOnClickListener(v -> {
            if (isExpanded) {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                dataList.add(new WeekItem(prevDayName , "потрачено: " + df.format(prevDaySpent  * curs.rate) + curs.symbol, "из: " + df.format(prevDayMustDo  * curs.rate) + curs.symbol));
                dataList.add(new WeekItem(currentDayName + " (сегодня)", "потрачено: " + df.format(todaySpent * curs.rate) + curs.symbol, "из: " + df.format(todayMustDo  * curs.rate) + curs.symbol));
                dataList.add(new WeekItem(nextDayName, "потрачено: " + df.format(nextDaySpent * curs.rate) + curs.symbol, "из: " + df.format(nextDayMustDo * curs.rate) + curs.symbol));

                isExpanded = false;
                podskazka.setVisibility(View.GONE);
                btnExpandList.setText("Расширить список");
                listView.getLayoutParams().height -= 700; // Увеличиваем высоту
            } else {
                while (dataList.size() > 0) {
                    dataList.remove(dataList.size() - 1);
                }
                int day = DayAdapter.getStartOfWeek();
                String[] weekDays = {"понедельник", "вторник", "среда", "четверг" , "пятница", "суббота", "воскресение"};
                for (int i = 0; i < 7; i++) {
                    dataList.add(new WeekItem(weekDays[i] , "потрачено: " + df.format(databaseHelper.getDoneSpents(day+i, day+i) * curs.rate) + curs.symbol,
                            "из: " + df.format(databaseHelper.getAllSpents(day+i, day+i) * curs.rate) + curs.symbol));
                }
                listView.getLayoutParams().height += 700; // Возвращаем высоту
                isExpanded = true;
                btnExpandList.setText("сократить список");
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

    private void deleteOldNotifications() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getReminderList();

        long currentTime = System.currentTimeMillis();
        long thresholdTime = currentTime - 2 * 60 * 1000; // 5 минут в миллисекундах

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

            if (timestamp < thresholdTime) {
                databaseHelper.deleteReminder(id);
            }
        }

        cursor.close();
    }




    private void showNotifCount(){
        TextView badge = findViewById(R.id.notification_badge);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getReminderList();

        int count = cursor.getCount();
        cursor.close();

        if (count > 0) {
            badge.setText(String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }



    //если пришло время дабавляем сумму дохода
    //исправить , бюджет щхитает не правильно
    private void refreshBudget() {
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Cursor income = databaseIncome.getIncomeList();
        List<BudgetItem> itemsToConfirm = new ArrayList<>();
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
                        itemsToConfirm.add(new BudgetItem(name, day, incomeNum, true));
                    }

                    if (checkOnce) {
                        databaseIncome.deactivateIncome(name , day);
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
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date currentDate = new Date();
                    Date givenDate = sdf.parse(timeToGive);

                    if (givenDate != null && !currentDate.before(givenDate)) {
                        itemsToConfirm.add(new BudgetItem(name, day, spentNum, false));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (spents.moveToNext());
        }
        if (!itemsToConfirm.isEmpty()) {
            checkGotten(itemsToConfirm);
        }

    }





    @SuppressLint("SetTextI18n")
    private void refreshBudgetText() {
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        double original = databaseIncome.getBudget();
        double converted = original * curs.rate;
        double income = databaseIncome.getIncome();
        if (income / 10 > original){
            budgetText.setTextColor(Color.RED);
        }
        String result = String.format("%.2f %s", converted, curs.symbol);
        budgetText.setText(result);
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
            remembring();
        }
    }

    private void checkGotten(List<BudgetItem> items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтвердите операции");

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (BudgetItem item : items) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText((item.isIncome ? "Доход: " : "Трата: ") + item.name + " - " + item.amount);
            layout.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        builder.setView(scrollView);

        builder.setPositiveButton("Подтвердить", (dialog, which) -> {
            DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
            for (int i = 0; i < items.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    BudgetItem item = items.get(i);
                    if (item.isIncome) {
                        int cnt = databaseIncome.setIncomeGiven(item.name, item.date);
                        databaseIncome.addIncome(cnt * item.amount);
                    } else {
                        databaseIncome.addSpent(item.amount);
                        databaseIncome.setMonthlySpentGiven(item.name, item.date);
                    }
                }
            }
            refreshBudgetText();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.my_cyan));
        });

        dialog.show();
    }



    private void remembring() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
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
                    if(today >= day) {
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
            builder.setTitle(Html.fromHtml("<font color='#1EFF00'>у вас есть не выполненые траты</font>"));
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
                                if(today >= itemDay) {
                                    databaseHelper.setDone(itemName, itemDay, 0, true);
                                    databaseIncome.addSpent(itemSpent);
                                    refreshBudgetText();
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

