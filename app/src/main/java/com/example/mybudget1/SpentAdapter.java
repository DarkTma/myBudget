package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class SpentAdapter extends BaseAdapter {
    private Context context;
    private List<SpentItem> incomeList;
    private int selectedPosition = -1;


    public SpentAdapter(Context context, List<SpentItem> incomeList) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.spent_item, parent, false);
        }

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        SpentItem spent = incomeList.get(position);

        TextView tvName = convertView.findViewById(R.id.tvSpentName);
        TextView tvAmount = convertView.findViewById(R.id.tvSpentAmount);
        TextView tvDate = convertView.findViewById(R.id.tvSpentDate);
        ImageButton btnedit = convertView.findViewById(R.id.btnEditSpent);
        ImageButton btncheck = convertView.findViewById(R.id.btnCheckSpent);
        ImageButton btndelete = convertView.findViewById(R.id.btnDeleteSpent);

        btnedit.setFocusable(false);
        btndelete.setFocusable(false);


        btncheck.setOnClickListener(v -> {
            showPaymentDialog(context, spent);
        });

        tvName.setText(spent.getName());
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double amaunt = Math.round((spent.getAmount() * curs.rate) * 100.0) / 100.0;
        tvAmount.setText("Сумма: " + amaunt + " " + curs.symbol);
        tvDate.setText("Дата: " + spent.getDate());

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


        btnedit.setOnClickListener(view -> {
            String itemName = spent.getName();
            double itemIncome = spent.getAmount();
            final int[] selectedCategoryId = {0};

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

            // Создаем `EditText` для суммы
            EditText inputSpent = new EditText(context);
            inputSpent.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputSpent.setText(String.valueOf(itemIncome));
            inputSpent.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputSpent.setBackgroundResource(R.drawable.edit_text_style_orange);

            LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spentParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputSpent.setLayoutParams(spentParams);


            // Создаем `Spinner` для выбора валюты
            Spinner currencySpinner = new Spinner(context);
            String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

            // Применяем стиль к Spinner
            currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
            currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

            // Получаем текущую валюту из базы данных
            String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты

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

            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            FileHelper fileHelper = new FileHelper(context);
            List<String> list = fileHelper.getAllCategories();

            List<String> categories = fileHelper.getAllCategories(); // Чтение категорий
            Spinner categorySpinner = new Spinner(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            categorySpinner.setBackgroundResource(R.drawable.spinner_bg);

            LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spinnerParams.setMargins(0, 20, 0, 20);
            categorySpinner.setPadding(20, 40, 40, 40);
            categorySpinner.setLayoutParams(spinnerParams);




            int category_id = databaseIncome.getCategoryId(spent.getId());
            categorySpinner.setSelection(categories.indexOf(fileHelper.getCategoryNameById(category_id)));

            // Обработчик выбора категории
            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // Сохраняем выбранную категорию
                    selectedCategoryId[0] = position;  // Записываем выбранный индекс категории
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            // Контейнер для `EditText` и `Spinner`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(currencySpinner); // Добавляем `Spinner` для валюты

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                int newIncome = Integer.parseInt(inputSpent.getText().toString());

                // Получаем выбранную валюту из `Spinner`
                String selectedCurrency = currencySpinner.getSelectedItem().toString();
                double finalAmount = 0;

                switch (selectedCurrency) {
                    case "֏": // Армянский драм
                        finalAmount = newIncome / CursHelper.getToDram();
                        break;
                    case "$": // Доллар США
                        finalAmount = newIncome / CursHelper.getToDollar();
                        break;
                    case "₽": // Российский рубль
                        finalAmount = newIncome / CursHelper.getToRub();
                        break;
                    case "元": // Китайский юань
                        finalAmount = newIncome / CursHelper.getToJuan();
                        break;
                    case "€": // Евро
                        finalAmount = newIncome / CursHelper.getToEur();
                        break;
                    case "¥": // Японская иена
                        finalAmount = newIncome / CursHelper.getToJen();
                        break;
                    case "₾": // Грузинский лари
                        finalAmount = newIncome / CursHelper.getToLari();
                        break;
                    default:
                        finalAmount = newIncome;
                        break;
                }

                finalAmount = Math.round(finalAmount * 100.0) / 100.0;

                // Обновляем запись в базе данных
                databaseIncome.updateMonthlySpent(spent.getId(), newName, finalAmount,selectedCategoryId[0]);


                spent.change(newName , finalAmount , spent.getDate());

                CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                String currentDate = sdf.format(new Date());
                databaseHelper.saveNote(currentDate, "Изменен ежемесячный расход\n" + itemName + " - " + itemIncome + cursd.symbol + "\nна: " + newName + " - " + finalAmount + cursd.symbol, "Spent", "edit" );
                notifyDataSetChanged();

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            builder.show();
        });


        btndelete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF5500'>Внимание</font>"));

            // Текст для описания
            TextView textView = new TextView(context);
            textView.setText("Хотите удалить расход?");
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            textView.setTextSize(24);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(0, 20, 0, 20);
            textView.setLayoutParams(textParams);


            // Layout для текста и чекбокса
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(textView);

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("далее");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String name = spent.getName();
                double amount = spent.getAmount();

                    databaseIncome.deleteMonthlySpent(spent.getId());

                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    databaseHelper.saveNote(currentDate, "удален регулярный расход:\n" + name + " - " + amount + cursd.symbol, "Spent", "delete");
                    incomeList.remove(position);
                    notifyDataSetChanged();

            });

            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            dialog.show();
        });




        return convertView;
    }

    private void showPaymentDialog(Context context, SpentItem spent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH); // 0-11
        int day = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, null, year, month, day);

        // Ограничиваем выбор от первого дня прошлого месяца до последнего дня текущего месяца
        Calendar minDate = (Calendar) today.clone();
        minDate.add(Calendar.MONTH, -1);
        minDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar maxDate = (Calendar) today.clone();
        maxDate.set(Calendar.DAY_OF_MONTH, maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        builder.setView(datePickerDialog.getDatePicker());
        builder.setTitle("Выполнение траты")
                .setMessage("Хотите сейчас же выполнить ежемесячный расход?")
                .setPositiveButton("Да", (dialog, which) -> {
                    int selectedDay = datePickerDialog.getDatePicker().getDayOfMonth();
                    int selectedMonth = datePickerDialog.getDatePicker().getMonth();
                    int selectedYear = datePickerDialog.getDatePicker().getYear();

                    // Определяем offset: 0 — текущий месяц, -1 — прошлый
                    int offset = 0;
                    if (selectedYear < year || (selectedYear == year && selectedMonth < month)) {
                        offset = -1;
                    }

                    // Выполняем запись
                    DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);

                    databaseHelper.insertData(selectedDay, spent.getName(), spent.getAmount(), offset, true, spent.getCategory());
                    databaseIncome.addSpent(spent.getAmount());

                    CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    String currentDate = sdf.format(new Date());

                    databaseHelper.saveNote(currentDate, "сделан ежемесячный расход: " + spent.getName() + " - " + spent.getAmount() + cursd.symbol, "Spent", "add");
                    Toast.makeText(context, "Успех", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Устанавливаем фон
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Меняем цвет кнопок после показа диалога
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
    }

}
