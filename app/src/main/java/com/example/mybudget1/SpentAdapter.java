package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Context;
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
import java.util.Calendar;
import java.util.List;

public class SpentAdapter extends BaseAdapter {
    private Context context;
    private List<SpentItem> incomeList;
    private boolean isMonthly;
    private int selectedPosition = -1; // -1 = ничего не выбрано


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
        ImageButton btndelete = convertView.findViewById(R.id.btnDeleteSpent);

        btnedit.setFocusable(false);
        btndelete.setFocusable(false);


        convertView.setOnLongClickListener(v -> {
            showPaymentDialog(context, spent);
            return true;
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

            // Создаем `NumberPicker` для дня получения
            NumberPicker inputDay = new NumberPicker(context);
            inputDay.setMinValue(1);
            inputDay.setMaxValue(31);
            inputDay.setValue(Integer.valueOf(spent.getDate()));

            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputParams.setMargins(0, 10, 0, 20);
            inputDay.setLayoutParams(inputParams);

            // Создаем `Spinner` для выбора валюты
            Spinner currencySpinner = new Spinner(context);
            String[] currencies = {"֏", "$", "₽"};
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

            // Применяем стиль к Spinner
            currencySpinner.setBackgroundResource(R.drawable.spinner_background_cyan);
            currencySpinner.setPopupBackgroundResource(R.drawable.spinner_background_cyan);

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

            // Контейнер для `EditText` и `Spinner`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(inputDay);
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
                int day = Integer.parseInt(String.valueOf(inputDay.getValue()));

                // Получаем выбранную валюту из `Spinner`
                String selectedCurrency = currencySpinner.getSelectedItem().toString();
                double finalAmount = 0;

                // Конвертируем сумму в выбранную валюту
                switch (selectedCurrency) {
                    case "֏":
                        finalAmount = newIncome / CursHelper.getToDram();
                        break;
                    case "$":
                        finalAmount = newIncome / CursHelper.getToDollar();
                        break;
                    case "₽":
                        finalAmount = newIncome / CursHelper.getToRub();
                        break;
                }

                finalAmount = Math.round(finalAmount * 100.0) / 100.0;

                // Обновляем запись в базе данных
                databaseIncome.updateMonthlySpent(itemName, day, newName, finalAmount);
                databaseIncome.addSpent(itemIncome);
                databaseIncome.addIncome( finalAmount);

                // Обновляем данные в списке и уведомляем адаптер
                spent.change(newName , finalAmount , String.valueOf(day));
                notifyDataSetChanged();

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            builder.show();
        });


        btndelete.setOnClickListener( view -> {
                    Calendar calendar = Calendar.getInstance();
                    int today = calendar.get(Calendar.DAY_OF_MONTH);
                    String name = spent.getName();
                    double count = spent.getAmount();
                    int day = Integer.parseInt(spent.getDate());
                    databaseIncome.deleteMonthlySpent(name, day);
                    if (day <= today){
                        databaseIncome.addIncome(count);
                    }
                    incomeList.remove(position);
                    notifyDataSetChanged();
            });


        return convertView;
    }

    private void showPaymentDialog(Context context, SpentItem spent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Выпалнение траты")
                .setMessage("Хотите сейчас же выполнить трату?")
                .setPositiveButton("Да", (dialog, which) -> {
                    DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                    databaseIncome.setMonthlySpentGiven(spent.getName(), spent.getDate());
                    databaseIncome.addSpent(spent.getAmount());
                    Toast.makeText(context, "Успех", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show(); // Показываем диалог перед изменением стиля

        // Устанавливаем фон
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Меняем цвет кнопок после показа диалога
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
    }
}
