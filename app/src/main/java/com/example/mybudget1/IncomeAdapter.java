package com.example.mybudget1;

import android.annotation.SuppressLint;
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
        CheckBox checkBox = convertView.findViewById(R.id.incomecheckbox);

        btnedit.setFocusable(false);
        btndelete.setFocusable(false);
        checkBox.setFocusable(false);

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
            checkBox.setChecked(isMonthly);
        } else {
            Log.e("DB_ERROR", "Cursor is empty or null for name: " + a);
        }

        if (cursor != null) {
            cursor.close(); // Закрываем Cursor
        }

        checkBox.setChecked(isMonthly);


        if (checkBox.isChecked()){
            monthly.setText("ежемесячная");
            monthly.setTextColor(Color.GREEN);
        } else {
            int color = ContextCompat.getColor(context, R.color.my_orange);
            monthly.setText("разовая");
            monthly.setTextColor(color);
        }

        convertView.setOnLongClickListener(v -> {
            showPaymentDialog(context, income);
            return true; // true означает, что событие обработано
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

            // Создаем `NumberPicker` для дня получения
            NumberPicker inputDay = new NumberPicker(context);
            inputDay.setMinValue(1);
            inputDay.setMaxValue(31);
            inputDay.setValue(Integer.valueOf(income.getDate()));

            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputParams.setMargins(0, 10, 0, 20);
            inputDay.setLayoutParams(inputParams);

            // Создаем спиннер для выбора валюты
            Spinner currencySpinner = new Spinner(context);
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{"Dram", "Dollar", "Rubli"});
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);

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
            layout.addView(inputDay);
            layout.addView(currencySpinner);  // Добавляем спиннер для валюты

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                double newIncome = Double.parseDouble(inputSpent.getText().toString());
                int day = Integer.parseInt(String.valueOf(inputDay.getValue()));

                // Получаем выбранную валюту из спиннера
                String selectedCurrency = currencySpinner.getSelectedItem().toString();

                double finalIncome = 0;
                // Преобразуем сумму в выбранную валюту
                switch (selectedCurrency) {
                    case "Dram":
                        finalIncome = newIncome / CursHelper.getToDram();
                        break;
                    case "Dollar":
                        finalIncome = newIncome / CursHelper.getToDollar();
                        break;
                    case "Rubli":
                        finalIncome = newIncome / CursHelper.getToRub();
                        break;
                }

                finalIncome = Math.round(finalIncome * 100.0) / 100.0;

                // Обновляем запись в базе данных
                databaseIncome.updateData(itemName, day, newName, finalIncome);

                databaseIncome.addSpent(itemIncome);
                databaseIncome.addIncome(finalIncome);

                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                String currentDate = sdf.format(new Date());
                databaseHelper.saveNote(currentDate, "Изменен доход:\n" + itemName + " - " + itemIncome + cursd.symbol + "\nна: " + newName + " - " + finalIncome + cursd.symbol, "Income", "edit" );

                income.change(newName, finalIncome, day);
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
            textView.setText("хотите полностю удалить доход? в этом месяце если \nвы его получали то он онулируется, если вы нехотите этого просто сделайте \nдоход одноразовым и в сле месяце он пропадет");
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            textView.setTextSize(24);

            // Устанавливаем параметры для текста
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(0, 20, 0, 20); // Отступы
            textView.setLayoutParams(textParams);

            // Чекбокс для выбора
            CheckBox checkBoxAsk = new CheckBox(context);
            checkBoxAsk.setText("Удалить полностью?");
            checkBoxAsk.setTextColor(ContextCompat.getColor(context, R.color.my_red));
            checkBoxAsk.setChecked(true);
            checkBoxAsk.setButtonDrawable(R.drawable.checkbox_style);

            // Устанавливаем параметры для чекбокса
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            checkBoxParams.setMargins(0, 20, 0, 20); // Отступы
            checkBoxAsk.setLayoutParams(checkBoxParams);

            // Размещаем все элементы в LinearLayout
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(textView);
            layout.addView(checkBoxAsk);

            // Устанавливаем виджеты в диалог
            builder.setView(layout);

            // Настройка текста для кнопок
            SpannableString positiveButtonText = new SpannableString("далее");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Добавить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                if (checkBoxAsk.isChecked()) {
                    String name = income.getName();
                    int day = income.getDate();
                    double incomen = income.getAmount();
                    databaseIncome.deleteIncome(name, day);  // Удаление дохода из базы данных

                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    databaseHelper.saveNote(currentDate, "удален доход:\n" + name + " - " + incomen + cursd.symbol, "Income", "delete" );

                    incomeList.remove(position); // Удаляем объект из списка
                    notifyDataSetChanged(); // Обновляем адаптер
                } else {
                    String name = income.getName();
                    databaseIncome.setMonthly(name, false); // Сделать доход одноразовым
                    notifyDataSetChanged(); // Обновляем адаптер
                }
            });

            // Кнопка "Отмена"
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            // Создание и показ диалога
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
            dialog.show();
        });

        checkBox.setOnClickListener(view -> {
            String name = income.getName();
            boolean isMonthlyy = checkBox.isChecked();
            double incomen = income.getAmount();

            new AlertDialog.Builder(view.getContext())
                    .setTitle("Подтвердите действие")
                    .setMessage(isMonthlyy
                            ? "Сделать эту запись ежемесячной?"
                            : "Сделать эту запись одноразовой?")
                    .setPositiveButton("Подтвердить", (dialog, which) -> {
                        databaseIncome.setMonthly(name, isMonthlyy);

                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        String stat = isMonthlyy ? "ежемесячный" : "одноразовый";
                        databaseHelper.saveNote(currentDate, "Статус дохода:\n" + name + " - " + incomen + cursd.symbol + "\nизменен на " + stat, "Income", "edit" );

                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        // Отменяем изменение чекбокса визуально
                        checkBox.setChecked(!isMonthlyy);
                    })
                    .show();
        });





        return convertView;
    }
    private void showPaymentDialog(Context context, IncomeItem income) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Выплата зарплаты")
                .setMessage("Хотите сейчас же выплатить зарплату?")
                .setPositiveButton("Да", (dialog, which) -> {
                    DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                    databaseIncome.setIncomeGiven(income.getName(), income.getDate());
                    databaseIncome.addIncome(income.getAmount());
                    Toast.makeText(context, "Зарплата выплачена!", Toast.LENGTH_SHORT).show();
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
