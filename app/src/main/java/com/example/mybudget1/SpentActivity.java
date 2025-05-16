package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intentGoBack = new Intent(SpentActivity.this, StartActivity.class);
                startActivity(intentGoBack);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        listViewIncome = findViewById(R.id.listViewSpent);
        btnBack = findViewById(R.id.buttonBackFromSpents);
        btnAddSpent = findViewById(R.id.btnAddMonthlySpent);
        tvSpent = findViewById(R.id.tvSpentM);
        refreshSpentText();


        btnAddSpent.setOnClickListener(v -> {
            final int[] selectedCategoryId = {0};
            final int[] selectedDay = new int[1];
            final int[] selectedOffset = new int[1];

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

            Calendar now = Calendar.getInstance(); // Сегодняшняя дата

            // Календарь для выбранной даты (инициализируем сегодняшней датой)
            final Calendar selectedDate = (Calendar) now.clone();

            // TextView для даты
            TextView dateTextView = new TextView(this);
            dateTextView.setTextSize(18f);
            dateTextView.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            dateTextView.setPadding(20, 30, 20, 30);
            dateTextView.setGravity(Gravity.CENTER);
            dateTextView.setBackgroundResource(R.drawable.edit_text_style);
            // Устанавливаем начальную дату (сегодня)
            updateDateText(dateTextView, selectedDate);

            dateTextView.setOnClickListener(view -> {

                // Минимум: 1-е число прошлого месяца
                Calendar minDate = (Calendar) now.clone();
                minDate.add(Calendar.MONTH, -1);
                minDate.set(Calendar.DAY_OF_MONTH, 1);

                // Максимум: последнее число следующего месяца
                Calendar maxDate = (Calendar) now.clone();
                maxDate.add(Calendar.MONTH, 1);
                maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        (picker, year, month, dayOfMonth) -> {
                            selectedDate.set(year, month, dayOfMonth);
                            updateDateText(dateTextView, selectedDate);

                            selectedDay[0] = dayOfMonth;

                            int currentMonth = now.get(Calendar.MONTH);
                            if (month == currentMonth - 1 || (currentMonth == 0 && month == 11)) {
                                selectedOffset[0] = -1;
                            } else if (month == currentMonth) {
                                selectedOffset[0] = 0;
                            } else {
                                selectedOffset[0] = 1;
                            }
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                );

                DatePicker picker = datePickerDialog.getDatePicker();
                picker.setMinDate(minDate.getTimeInMillis());
                picker.setMaxDate(maxDate.getTimeInMillis());

                datePickerDialog.show();
            });

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

            Spinner frequencySpinner = new Spinner(this);
            String[] frequencyOptions = {"Раз в месяц", "Раз в неделю", "Другое"};
            ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencyOptions);
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            frequencySpinner.setAdapter(frequencyAdapter);

            currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
            currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

            frequencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
            frequencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

            LinearLayout.LayoutParams freqParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            freqParams.setMargins(0, 10, 0, 20);
            frequencySpinner.setLayoutParams(freqParams);


            EditText customDaysEdit = new EditText(this);
            customDaysEdit.setHint("Раз в сколько дней?");
            customDaysEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
            customDaysEdit.setVisibility(View.GONE); // Скрыт по умолчанию
            customDaysEdit.setBackgroundResource(R.drawable.edit_text_style);
            customDaysEdit.setPadding(0, 30, 0, 10);
            LinearLayout.LayoutParams customDaysParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            customDaysParams.setMargins(0, 10, 0, 20);
            customDaysEdit.setLayoutParams(customDaysParams);


            frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 2) { // "Другое"
                        customDaysEdit.setVisibility(View.VISIBLE);
                    } else {
                        customDaysEdit.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            FileHelper fileHelper = new FileHelper(this);
            List<String> categories = fileHelper.getAllCategories();

            Spinner categorySpinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);

            categorySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);
            categorySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
            categorySpinner.setDropDownVerticalOffset(10);
            categorySpinner.setSelection(categories.indexOf("прочее"));

            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spinnerParams.setMargins(0, 20, 0, 20);
            categorySpinner.setLayoutParams(spinnerParams);

            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    selectedCategoryId[0] = position + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {}
            });

            // Контейнер для всех элементов
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(customTitle);
            layout.addView(name);
            layout.addView(spent);
            layout.addView(dateTextView);
            layout.addView(currencySpinner);
            layout.addView(categorySpinner);
            layout.addView(frequencySpinner);
            layout.addView(customDaysEdit);

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

                            String frequencyChoice = frequencySpinner.getSelectedItem().toString();
                            int customRepeatDays = 0;

                            switch (frequencyChoice) {
                                case "Раз в месяц":
                                    customRepeatDays = 0;
                                    break;
                                case "Раз в неделю":
                                    customRepeatDays = 7;
                                    break;
                                case "Другое":
                                    String customDaysText = customDaysEdit.getText().toString().trim();
                                    if (customDaysText.isEmpty() || Integer.parseInt(customDaysText) <= 0) {
                                        Toast.makeText(this, "Введите корректное количество дней", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    customRepeatDays = Integer.parseInt(customDaysText);
                                    break;
                            }

                            int spentValue = Integer.parseInt(spent.getText().toString());
                            int dayText = selectedDay[0];
                            int offset = selectedOffset[0];

                            // Получаем выбранную валюту из Spinner
                            String selectedCurrency = currencySpinner.getSelectedItem().toString();
                            double finalAmount = 0;

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
                                    finalAmount = spentValue;
                                    break;
                            }

                            finalAmount = Math.round(finalAmount * 10000.0) / 10000.0;


                            // Обновляем запись в базе данных
                            databaseIncome.addMonthlySpent(nameText, finalAmount, dayText , customRepeatDays, selectedCategoryId[0],offset);

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
                String nextDate = income.getString(income.getColumnIndexOrThrow("next"));
                if ("01-01-3000".equals(nextDate)) {
                    continue; // пропускаем деактивированные записи
                }
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                int id = income.getInt(income.getColumnIndexOrThrow("id"));
                double spentNum = income.getDouble(income.getColumnIndexOrThrow("monthly_spent"));
                int date = income.getInt(income.getColumnIndexOrThrow("spentday"));
                int category_id = income.getInt(income.getColumnIndexOrThrow("category_id"));
                spentList.add(new SpentItem(id,name, spentNum, date,nextDate,category_id));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new SpentAdapter(this, spentList);
        listViewIncome.setAdapter(adapter);
    }
    private void updateDateText(TextView dateTextView, Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // месяцы с 0
        int year = calendar.get(Calendar.YEAR);
        String dateStr = String.format("%02d.%02d.%04d", day, month, year);
        dateTextView.setText(dateStr);
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

