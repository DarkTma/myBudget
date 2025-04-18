package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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

public class IncomeActivity extends AppCompatActivity {

    private ListView listViewIncome;
    private IncomeAdapter adapter;
    private Button btnAddIncome;
    private ArrayList<IncomeItem> incomeList;
    private ImageButton btnBack;
    private TextView incomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        listViewIncome = findViewById(R.id.listViewIncome);
        btnBack = findViewById(R.id.buttonBackFromIncome);
        btnAddIncome = findViewById(R.id.btnAddIncome);
        incomeText = findViewById(R.id.tvIncome);

        refreshIncomeText();
        refreshList();

        btnAddIncome.setOnClickListener(v -> {
            TextView customTitle = new TextView(this);
            customTitle.setText("Добавить доход");
            customTitle.setTextSize(20);
            customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            customTitle.setPadding(0, 20, 0, 20);
            customTitle.setGravity(Gravity.CENTER);

            EditText name = new EditText(this);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint("Название дохода");
            name.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            name.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            name.setLayoutParams(nameParams);

            // Создаем EditText для ввода дохода (с плавающей запятой)
            EditText income = new EditText(this);
            income.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            income.setHint("Доход");
            income.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            income.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams incomeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            incomeParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            income.setLayoutParams(nameParams);

            // Ограничение ввода до 2 знаков после запятой
            income.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    String text = editable.toString();
                    if (text.contains(".")) {
                        int dotIndex = text.indexOf(".");
                        if (text.length() - dotIndex > 3) {
                            editable.replace(dotIndex + 3, editable.length(), "");
                        }
                    }
                }
            });

            // Создаем NumberPicker для дня
            NumberPicker day = new NumberPicker(this);
            day.setMinValue(1);
            day.setMaxValue(31);
            day.setWrapSelectorWheel(true); // Цикличная прокрутка

            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dayParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            day.setLayoutParams(dayParams);

            // Создаем CheckBox для ежемесячного дохода
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText("Ежемесячный доход?");
            checkBox.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            checkBox.setChecked(true);
            checkBox.setButtonDrawable(R.drawable.checkbox_style);

            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            checkBoxParams.setMargins(0, 20, 0, 20); // Устанавливаем отступы
            checkBox.setLayoutParams(checkBoxParams);

            // Создаем Spinner для выбора валюты
            Spinner currencySpinner = new Spinner(this);
            String[] currencies = {"֏", "$", "₽"};
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

            // Получаем текущую валюту из базы данных
            String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты

            // Преобразуем символ в валютный знак и выбираем в Spinner
            int defaultCurrencyPosition = 0; // Изначально установим на 0 (например, драм)
            switch (currentCurrencySymbol) {
                case "dollar":
                    defaultCurrencyPosition = 1;
                    break;
                case "rubli":
                    defaultCurrencyPosition = 2;
                    break;
                case "dram":
                default:
                    defaultCurrencyPosition = 0;
                    break;
            }
            currencySpinner.setSelection(defaultCurrencyPosition); // Устанавливаем валюту по умолчанию

            // Создаем контейнер для всех элементов
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(customTitle);
            layout.addView(name);
            layout.addView(income);
            layout.addView(day);
            layout.addView(checkBox);
            layout.addView(currencySpinner); // Добавляем Spinner для валюты

            // Создаем и показываем AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"))
                    .setView(layout)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        if (!income.getText().toString().isEmpty() && Double.parseDouble(income.getText().toString()) != 0) {
                            String nameText = name.getText().toString().trim();
                            if (nameText.isEmpty()) {
                                nameText = "Доход";
                            }

                            double incomeText = Double.parseDouble(income.getText().toString());

                            int dayText = day.getValue();

                            if (dayText > 31) {
                                Toast.makeText(this, "Можно выбрать до 31-го числа", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                return;
                            }

                            boolean once = checkBox.isChecked(); // Обратная логика: если чекбокс НЕ отмечен, то once = false

                            // Получаем выбранную валюту из Spinner
                            String selectedCurrency = currencySpinner.getSelectedItem().toString();
                            double finalIncome = 0;

                            // Конвертируем сумму в выбранную валюту
                            switch (selectedCurrency) {
                                case "֏":
                                    finalIncome = incomeText / CursHelper.getToDram();
                                    break;
                                case "$":
                                    finalIncome = incomeText / CursHelper.getToDollar();
                                    break;
                                case "₽":
                                    finalIncome = incomeText / CursHelper.getToRub();
                                    break;
                            }

                            finalIncome = Math.round(finalIncome * 100.0) / 100.0;

                            // Записываем в базу
                            databaseIncome.setIncome(finalIncome, nameText, dayText, once);
                            CursData curs = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                            String currentDate = sdf.format(new Date());
                            DatabaseHelper databaseHelper = new DatabaseHelper(this);
                            databaseHelper.saveNote(currentDate, "добавлен новый доход: " + nameText + " - " + finalIncome + curs.symbol, "Income", "add" );

                            dialog.dismiss();

                            refreshIncomeText();
                            refreshList();
                        } else {
                            Toast.makeText(this, "Вы не добавили доход", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(negativeButtonText, null);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
            dialog.show();
        });


        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(IncomeActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });


        // Заполняем список тестовыми данными

    }

    private void refreshList() {
        incomeList = new ArrayList<>();
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        Cursor income = databaseIncome.getIncomeList();
        if (income != null && income.moveToFirst()) {
            do {
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                double incomeNum = income.getDouble(income.getColumnIndexOrThrow("income"));
                int date = income.getInt(income.getColumnIndexOrThrow("incomeday"));
                String once = income.getString(income.getColumnIndexOrThrow("onceincome"));
                boolean x = false;
                if (once.equals("1")) {
                    x = true;
                }
                incomeList.add(new IncomeItem(name, incomeNum, date, x));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new IncomeAdapter(this, incomeList);
        listViewIncome.setAdapter(adapter);
    }

    private void refreshIncomeText() {
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
        int incomeAll = databaseIncome.getIncome();
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double converted = incomeAll * curs.rate;
        String result = String.format("%.2f %s", converted, curs.symbol);
        if (incomeAll == 0){
            incomeText.setText("доход: ?");
        } else {
            incomeText.setText("доход: " + result);
        }
    }
}
