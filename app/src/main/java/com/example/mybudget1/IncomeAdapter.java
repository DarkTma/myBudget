package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.mybudget1.IncomeItem;
import com.example.mybudget1.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomeAdapter extends BaseAdapter {
    private Context context;
    private List<IncomeItem> incomeList;
    private boolean isMonthly;
    private int selectedPosition = -1;

    public IncomeAdapter(Context context, List<IncomeItem> incomeList) {
        this.context = context;
        this.incomeList = incomeList;
    }

    @Override
    public int getCount() {
        return incomeList.size();
    }

    @Override
    public Object getItem(int position) {
        return incomeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.income_item, parent, false);
        }

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        IncomeItem income = incomeList.get(position);

        TextView tvName = convertView.findViewById(R.id.tvIncomeName);
        TextView tvAmount = convertView.findViewById(R.id.tvIncomeAmount);
        TextView tvDate = convertView.findViewById(R.id.tvIncomeDate);
        TextView monthly = convertView.findViewById(R.id.monthly);
        ImageButton btnedit = convertView.findViewById(R.id.btnEditIncome);
        ImageButton btndelete = convertView.findViewById(R.id.btnDeleteIncome);
        ImageButton btncheck = convertView.findViewById(R.id.btnCheckIncome);

        btnedit.setFocusable(false);
        btndelete.setFocusable(false);

        tvName.setText(income.getName());
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double amaunt = Math.round((income.getAmount() * curs.rate) * 100.0) / 100.0;
        tvAmount.setText("Сумма: " + amaunt + " " + curs.symbol);
        tvDate.setText("Дата: " + income.getDate());

        if (position == selectedPosition) {
            btnedit.setVisibility(View.VISIBLE);
            btndelete.setVisibility(View.VISIBLE);
        } else {
            btnedit.setVisibility(View.GONE);
            btndelete.setVisibility(View.GONE);
        }

        // Обработка клика по элементу
        convertView.setOnClickListener(v -> {
            selectedPosition = (selectedPosition == position) ? -1 : position;
            notifyDataSetChanged(); // обновить список
        });

        String a = income.getName().trim();
        Cursor cursor = databaseIncome.getMonthly(a);

        if (cursor != null && cursor.moveToFirst()) {
            int onceIncome = cursor.getInt(cursor.getColumnIndexOrThrow("onceincome"));
            isMonthly = onceIncome == 1;
            if (isMonthly) {
                monthly.setText("регулярный");
                monthly.setTextColor(Color.GREEN);
            } else {
                monthly.setText("одноразовый");
                int color = ContextCompat.getColor(context, R.color.my_orange);
                monthly.setTextColor(color);
            }

        } else {
            Log.e("DB_ERROR", "Cursor is empty or null for name: " + a);
        }

        if (cursor != null) {
            cursor.close();
        }


        btncheck.setOnClickListener(v -> {
            showPaymentDialog(context, income);
        });

        btnedit.setOnClickListener(view -> {
            String itemName = income.getName(); // "Coffee"
            double itemIncome = income.getAmount(); // "100.00"

            // Создаем всплывающее окно
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF5500'>Редактирование</font>"));

            // Создаем `EditText` для имени
            EditText inputName = new EditText(context);
            inputName.setInputType(InputType.TYPE_CLASS_TEXT);
            inputName.setText(itemName);
            inputName.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputName.setBackgroundResource(R.drawable.edit_text_style_orange);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputName.setLayoutParams(nameParams);

            // Создаем `EditText` для суммы (с плавающей запятой, до 2 знаков после запятой)
            EditText inputSpent = new EditText(context);
            inputSpent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputSpent.setText(String.format("%.2f", itemIncome));  // Отображаем число с двумя знаками после запятой
            inputSpent.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputSpent.setBackgroundResource(R.drawable.edit_text_style_orange);

            // Ограничение до двух знаков после запятой
            inputSpent.addTextChangedListener(new TextWatcher() {
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

            LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spentParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputSpent.setLayoutParams(nameParams);


            // Создаем спиннер для выбора валюты
            Spinner currencySpinner = new Spinner(context);
            String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

            String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты (например, "dram", "dollar", "rubli")

            // Преобразуем символ в валютный знак и выбираем в Spinner
            String selectedSymbol = currentCurrencySymbol; // Получаем символ текущей валюты
            int defaultCurrencyPosition = 0; // Изначально установим на 0 (например, драм)

            switch (selectedSymbol) {
                case "dollar":
                    defaultCurrencyPosition = 1;
                    break;
                case "rubli":
                    defaultCurrencyPosition = 2;
                    break;
                case "yuan":
                    defaultCurrencyPosition = 3;
                    break;
                case "evro":
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

            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            currencySpinner.setLayoutParams(spinnerParams);

            // Контейнер для `EditText`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(currencySpinner);

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                double newIncome = Double.parseDouble(inputSpent.getText().toString().replace(',', '.'));

                String selectedCurrency = currencySpinner.getSelectedItem().toString();
                double finalIncome = 0;

                switch (selectedCurrency) {
                    case "֏":
                        finalIncome = newIncome / CursHelper.getToDram();
                        break;
                    case "$":
                        finalIncome = newIncome / CursHelper.getToDollar();
                        break;
                    case "₽":
                        finalIncome = newIncome / CursHelper.getToRub();
                        break;
                    case "元":
                        finalIncome = newIncome / CursHelper.getToJuan();
                        break;
                    case "€":
                        finalIncome = newIncome / CursHelper.getToEur();
                        break;
                    case "¥":
                        finalIncome = newIncome / CursHelper.getToJen();
                        break;
                    case "₾":
                        finalIncome = newIncome / CursHelper.getToLari();
                        break;
                    default:
                        finalIncome = newIncome;
                        break;
                }

                finalIncome = Math.round(finalIncome * 100.0) / 100.0;

                databaseIncome.updateData(income.getId(), newName, finalIncome);

                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                String currentDate = sdf.format(new Date());
                databaseHelper.saveNote(currentDate, "Изменен доход:\n" + itemName + " - " + itemIncome + cursd.symbol + "\nна: " + newName + " - " + finalIncome + cursd.symbol, "Income", "edit" );

                income.change(newName, finalIncome, income.getDate());
                notifyDataSetChanged();

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            // Кнопка "Отмена"
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_edit); // Устанавливаем фон
            dialog.show();
        });




        btndelete.setOnClickListener( view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF5500'>Внимание</font>"));

            // Текст для описания
            TextView textView = new TextView(context);
            textView.setText("хотите удалить доход? ");
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            textView.setTextSize(24);

            // Устанавливаем параметры для текста
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(0, 20, 0, 20); // Отступы
            textView.setLayoutParams(textParams);

            // Размещаем все элементы в LinearLayout
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(textView);

            // Устанавливаем виджеты в диалог
            builder.setView(layout);

            // Настройка текста для кнопок
            SpannableString positiveButtonText = new SpannableString("далее");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Добавить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                    String name = income.getName();
                    double incomen = income.getAmount();

                    databaseIncome.deleteIncome(income.getId());

                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    databaseHelper.saveNote(currentDate, "удален доход:\n" + name + " - " + incomen + cursd.symbol, "Income", "delete" );

                    incomeList.remove(position); // Удаляем объект из списка
                    notifyDataSetChanged(); // Обновляем адаптер

            });

            // Кнопка "Отмена"
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            // Создание и показ диалога
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
            dialog.show();
        });


        return convertView;
    }
    private void showPaymentDialog(Context context, IncomeItem income) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH); // 0–11
        int day = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, null, year, month, day);

        // Мин. дата — первый день прошлого месяца
        Calendar minDate = (Calendar) today.clone();
        minDate.add(Calendar.MONTH, -1);
        minDate.set(Calendar.DAY_OF_MONTH, 1);

        // Макс. дата — последний день текущего месяца
        Calendar maxDate = (Calendar) today.clone();
        maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        builder.setView(datePickerDialog.getDatePicker());
        builder.setTitle("Выплата зарплаты")
                .setMessage("Хотите сейчас же выплатить зарплату?")
                .setPositiveButton("Да", (dialog, which) -> {
                    int selectedDay = datePickerDialog.getDatePicker().getDayOfMonth();
                    int selectedMonth = datePickerDialog.getDatePicker().getMonth();
                    int selectedYear = datePickerDialog.getDatePicker().getYear();

                    // Определяем offset: 0 — текущий месяц, -1 — прошлый
                    int offset = 0;
                    if (selectedYear < year || (selectedYear == year && selectedMonth < month)) {
                        offset = -1;
                    }

                    // Вставка данных
                    DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    databaseHelper.insertData(selectedDay, income.getName(), -1 * income.getAmount(), offset, true);
                    databaseIncome.addIncome(income.getAmount());
                    if (!income.monthly) {
                        databaseIncome.deleteIncome(income.getId());
                    }

                    Toast.makeText(context, "Зарплата выплачена!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Установка стиля
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Цвет кнопок
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
    }


}
