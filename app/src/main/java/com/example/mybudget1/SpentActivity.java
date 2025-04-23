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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SpentActivity extends AppCompatActivity {

    private ListView listViewIncome;
    private SpentAdapter adapter;
    private Button btnAddSpent;
    private ArrayList<SpentItem> spentList;
    private ImageButton btnBack;
    private TextView tvSpent;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_spents);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);



        listViewIncome = findViewById(R.id.listViewSpent);
        btnBack = findViewById(R.id.buttonBackFromSpents);
        btnAddSpent = findViewById(R.id.btnAddMonthlySpent);
        tvSpent = findViewById(R.id.tvSpentM);
        refreshSpentText();


        btnAddSpent.setOnClickListener(v -> {
            TextView customTitle = new TextView(this);
            customTitle.setText("Добавить трату");
            customTitle.setTextSize(20);
            customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            customTitle.setPadding(0, 20, 0, 20);
            customTitle.setGravity(Gravity.CENTER);

            EditText name = new EditText(this);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint("Название траты");
            name.setPadding(0, 30, 0, 10);
            name.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20);
            name.setLayoutParams(nameParams);

            EditText spent = new EditText(this);
            spent.setInputType(InputType.TYPE_CLASS_NUMBER);
            spent.setHint("Сумма");
            spent.setPadding(0, 30, 0, 10);
            spent.setBackgroundResource(R.drawable.edit_text_style);
            spent.setLayoutParams(nameParams);

            NumberPicker dayPicker = new NumberPicker(this);
            dayPicker.setMinValue(1);
            dayPicker.setMaxValue(31);
            dayPicker.setWrapSelectorWheel(true);

            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dayParams.setMargins(0, 10, 0, 20);
            dayPicker.setLayoutParams(dayParams);

            Spinner currencySpinner = new Spinner(this);
            String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};

            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

            // Получаем текущую валюту из базы данных
            String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты

            // Преобразуем символ в валютный знак и выбираем в Spinner
            int defaultCurrencyPosition;

            switch (currentCurrencySymbol) {
                case "dollar":
                    defaultCurrencyPosition = 1;
                    break;
                case "rubli":
                    defaultCurrencyPosition = 2;
                    break;
                case "yuan":
                    defaultCurrencyPosition = 3;
                    break;
                case "eur":
                    defaultCurrencyPosition = 4;
                    break;
                case "jen":
                    defaultCurrencyPosition = 5;
                    break;
                case "lari":
                    defaultCurrencyPosition = 6;
                    break;
                case "dram":
                default:
                    defaultCurrencyPosition = 0;
                    break;
            }

            currencySpinner.setSelection(defaultCurrencyPosition);

            // Контейнер для всех элементов
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(customTitle);
            layout.addView(name);
            layout.addView(spent);
            layout.addView(dayPicker);
            layout.addView(currencySpinner); // Добавляем Spinner для валюты

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"))
                    .setView(layout)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        if (!spent.getText().toString().isEmpty() && Integer.parseInt(spent.getText().toString()) != 0) {
                            String nameText = name.getText().toString().trim();
                            if (nameText.isEmpty()) {
                                nameText = "Трата";
                            }

                            int spentValue = Integer.parseInt(spent.getText().toString());
                            int selectedDay = dayPicker.getValue();

                            // Получаем выбранную валюту из Spinner
                            String selectedCurrency = currencySpinner.getSelectedItem().toString();
                            double finalAmount = 0;

// Конвертируем сумму в выбранную валюту
                            switch (selectedCurrency) {
                                case "֏":
                                    finalAmount = spentValue / CursHelper.getToDram();
                                    break;
                                case "$":
                                    finalAmount = spentValue / CursHelper.getToDollar();
                                    break;
                                case "₽":
                                    finalAmount = spentValue / CursHelper.getToRub();
                                    break;
                                case "元":
                                    finalAmount = spentValue / CursHelper.getToJuan();
                                    break;
                                case "€":
                                    finalAmount = spentValue / CursHelper.getToEur();
                                    break;
                                case "¥":
                                    finalAmount = spentValue / CursHelper.getToJen();
                                    break;
                                case "₾":
                                    finalAmount = spentValue / CursHelper.getToLari();
                                    break;
                                default:
                                    finalAmount = spentValue; // На случай, если символ неизвестен
                                    break;
                            }

                            finalAmount = Math.round(finalAmount * 100.0) / 100.0;


                            // Обновляем запись в базе данных
                            databaseIncome.addMonthlySpent(nameText, finalAmount, selectedDay);
                            CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                            String currentDate = sdf.format(new Date());
                            DatabaseHelper databaseHelper = new DatabaseHelper(this);
                            databaseHelper.saveNote(currentDate, "добавлен новый рассход: " + nameText + " - " + finalAmount + curs.symbol, "Spent", "add" );
                            dialog.dismiss();

                            Intent intent = new Intent(SpentActivity.this, SpentActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Вы не добавили трату", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(negativeButtonText, null);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
            dialog.show();
        });



        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(SpentActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });


        // Заполняем список тестовыми данными
        spentList = new ArrayList<>();
        Cursor income = databaseIncome.getMonthlySpentList();
        if (income != null && income.moveToFirst()) {
            do {
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                double spentNum = income.getDouble(income.getColumnIndexOrThrow("monthly_spent"));
                String date = income.getString(income.getColumnIndexOrThrow("spentday"));
                spentList.add(new SpentItem(name, spentNum, date));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new SpentAdapter(this, spentList);
        listViewIncome.setAdapter(adapter);
    }

    private void refreshSpentText() {
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        double spent = databaseIncome.getMonthlySpentSum();
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double converted = spent * curs.rate;
        String result = String.format("%.2f %s", converted, curs.symbol);
        tvSpent.setText("траты: " + result);
    }
}

