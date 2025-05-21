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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
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
    private WeekItemAdapter adapter;
    private List<String> dataList;
    private TextView spentText;
    public Button monthlySpents;
    public Button lastMonths;
    public Button geminiAnalizbtn;
    public TextView budgetText , savingsText , planText,incomeText;
    public CursData curs;
    private ActivityResultLauncher<Intent> authPermissionLauncher;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        spentText = findViewById(R.id.tvSpent);
        monthlySpents = findViewById(R.id.btnMonthlySpents);
        budgetText = findViewById(R.id.tvBudget);
        lastMonths = findViewById(R.id.btnLastMonths);
        geminiAnalizbtn = findViewById(R.id.btnGeminiGo);
        savingsText = findViewById(R.id.tvSavings);
        incomeText = findViewById(R.id.tvIncome);
        planText = findViewById(R.id.tvPlans);

        curs = CursHelper.getCursData(databaseIncome.getCurs());


        authPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(this, "Разрешение получено, повторите операцию", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Разрешение не получено", Toast.LENGTH_SHORT).show();
                    }
                }
        );


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

        ImageButton btnNotif = findViewById(R.id.notifications);
        btnNotif.setOnClickListener(v -> {
            startActivity(new Intent(this, ReminderListActivity.class));
        });

        checkMonth();

        double spent = databaseHelper.checkAllSpents(0);
        String result = String.format("%.2f %s", spent * curs.rate , curs.symbol);
        spentText.setText("расход: " + result);

        double savingsAmount = databaseHelper.getGoalsCurrentAmount();
        String resultS = String.format("%.2f %s", savingsAmount * curs.rate , curs.symbol);
        savingsText.setText("накопления: " + resultS);

        double plansAmount = -1 * databaseHelper.getSumOfNotDoneSpentsOfMonth();
        double convertedAmount = plansAmount * curs.rate;
        String sign = convertedAmount >= 0 ? "+" : "";
        String resultP = String.format("%s%.2f %s", sign, convertedAmount, curs.symbol);
        planText.setText("запланировано: " + resultP);

        double income = -1 * databaseHelper.getCurrentIncomesTotal();
        double convertedIncome = -1 * income * curs.rate;
        String resultI = String.format("%.2f %s", convertedIncome, curs.symbol);
        incomeText.setText("доход: " + resultI);

        //int budget = databaseIncome.controlBudget(income , spent);
        refreshBudgetText();
        refreshIncomesDatas();

        File dbFile = this.getDatabasePath("expenses.db");
        long lastModified = dbFile.lastModified();
        Log.d("DBCheck", "Last modified: " + new Date(lastModified).toString());

        RecyclerView recyclerView = findViewById(R.id.cardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        List<CardItem> cardItems = new ArrayList<>();
        cardItems.add(new CardItem(R.drawable.category, "Категории"));
        cardItems.add(new CardItem(R.drawable.income, "Доходы"));
        cardItems.add(new CardItem(R.drawable.oborot, "Оборот"));
        cardItems.add(new CardItem(R.drawable.spents, "Регулярные расходы"));
        cardItems.add(new CardItem(R.drawable.data, "Данные по месецам"));
        cardItems.add(new CardItem(R.drawable.currencies, "Валюты"));
        cardItems.add(new CardItem(R.drawable.maket, "Шаблоны"));
        cardItems.add(new CardItem(R.drawable.graf, "Графики"));
        cardItems.add(new CardItem(R.drawable.savings, "Накопления"));
        cardItems.add(new CardItem(R.drawable.asistent, "Асистент"));
        cardItems.add(new CardItem(R.drawable.history, "История"));
        cardItems.add(new CardItem(R.drawable.settings, "Настройки"));
        cardItems.add(new CardItem(R.drawable.scan, "Сканнер"));

        CardAdapter adapter = new CardAdapter(cardItems, item -> {
            switch (item.getText()) {
                case "Категории":
                    startActivity(new Intent(this, CategoriesActivity.class));
                    break;
                case "Доходы":
                    startActivity(new Intent(this, IncomeActivity.class));
                    break;
                case "Оборот":
                    startActivity(new Intent(this, DayActivity.class));
                    break;
                case "Регулярные расходы":
                    startActivity(new Intent(this, SpentActivity.class));
                    break;
                case "Данные по месецам":
                    startActivity(new Intent(this, MonthListActivity.class));
                    break;
                case "Валюты":
                    startActivity(new Intent(this, CurrencyActivity.class));
                    break;
                case "Шаблоны":
                    startActivity(new Intent(this, MaketListActivity.class));
                    break;
                case "Графики":
                    startActivity(new Intent(this, GraphActivity.class));
                    break;
                case "Накопления":
                    startActivity(new Intent(this, GoalActivity.class));
                    break;
                case "Асистент":
                    startActivity(new Intent(this, GeminiChatActivity.class));
                    break;
                case "История":
                    startActivity(new Intent(this, NotesActivity.class));
                    break;
                case "Настройки":
                    startActivity(new Intent(this, PrivacyPolicyActivity.class));
                    break;
                case "Сканнер":
                    startActivity(new Intent(this, ScanReceiptActivity.class));
                    break;
                default:
                    break;
            }
        });

        recyclerView.setAdapter(adapter);


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
                    e.printStackTrace();
                }

            } while (income.moveToNext());
        }


        Cursor spents = databaseIncome.getMonthlySpentList();
        if (spents != null && spents.moveToFirst()) {
            do {
                String name = spents.getString(spents.getColumnIndexOrThrow("name"));
                int spentNum = spents.getInt(spents.getColumnIndexOrThrow("monthly_spent"));
                int day = spents.getInt(spents.getColumnIndexOrThrow("spentday"));
                int category_id = spents.getInt(spents.getColumnIndexOrThrow("category_id"));
                String timeToGive = spents.getString(spents.getColumnIndexOrThrow("next"));

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date currentDate = new Date();
                    Date givenDate = sdf.parse(timeToGive);

                    if (givenDate != null && !currentDate.before(givenDate)) {
                        itemsToConfirm.add(new BudgetItem(name, day, spentNum, false,category_id));
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
        budgetText.setText("Bаланс: " + result);
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

            SharedPreferences prefs = this.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE);
            String savedEmail = prefs.getString("GOOGLE_ACCOUNT_EMAIL", null);

// Получаем аккаунт, если пользователь уже авторизован
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            BackupHelper backupHelper = new BackupHelper(this);
            File backupFile = backupHelper.createBackupZipFile();

            if (savedEmail != null && account != null && savedEmail.equals(account.getEmail())) {
                DriveHelper driveHelper = new DriveHelper(StartActivity.this, account,authPermissionLauncher);
                driveHelper.uploadBackupFile(backupFile);
            } else {
                Log.d("AutoBackup", "Аккаунт не совпадает или не найден, автосохранение не выполнено");
            }


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
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < items.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    BudgetItem item = items.get(i);
                    if (item.isIncome) {
                        int cnt = databaseIncome.setIncomeGiven(item.name, item.date);
                        databaseIncome.addIncome(cnt * item.amount);
                        for (int j = 0; j < cnt; j++) {
                            databaseHelper.insertData(day,item.name,-1 * item.amount,0,true);
                        }
                    } else {
                        databaseIncome.addSpent(item.amount);
                        int cnt = databaseIncome.setMonthlySpentGiven(item.name, item.date);
                        for (int j = 0; j < cnt; j++) {
                            databaseHelper.insertData(day,item.name,item.amount,0,true,item.category_id);
                        }
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
                    spent = -1 * allNoteDoneSpents.getInt(allNoteDoneSpents.getColumnIndexOrThrow("spent"));
                    day = allNoteDoneSpents.getInt(allNoteDoneSpents.getColumnIndexOrThrow("day"));
                    if(today >= day) {
                        String sign = spent >= 0 ? "+" : "";
                        dataList2.add(name + " : " + sign + spent + "₽ , " + day + " числа\n");
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


        databaseIncome.resetIncome();
        databaseIncome.resetMonthlySpents();

    }
}

