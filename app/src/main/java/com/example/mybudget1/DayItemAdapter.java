package com.example.mybudget1;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;


import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class DayItemAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;
    private int currentDay; // Добавляем текущий день, для которого отображаются элементы
    private Context activity;
    private int selectedPosition = -1;

    public DayItemAdapter(Context context, List<String> items, int currentDay) {
        super(context, R.layout.list_item, items);
        this.context = context;
        this.items = items;
        this.currentDay = currentDay; // Сохраняем текущий день
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView textViewItemName = convertView.findViewById(R.id.textViewItemName);
        TextView textViewItemPrice = convertView.findViewById(R.id.textViewItemPrice);
        ImageButton buttonEdit = convertView.findViewById(R.id.buttonEdit);
        ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);
        CheckBox checkBox = convertView.findViewById(R.id.isComplete);

        // Получаем данные элемента
        String itemText = items.get(position);
        String[] itemparts = itemText.split("-");

        if (itemparts.length < 3) {
            Log.e("Adapter", "Неверный формат данных: " + itemText);
            return convertView;
        }

        String name = itemparts[0];
        String price = itemparts[1];
        boolean isChecked = itemparts[2].equals("true");
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData data = CursHelper.getCursData(databaseIncome.getCurs());
        String priceString = price.replaceAll("[^\\d.]", "");
        double DefaultSpent = Double.parseDouble(priceString);
        double converted = DefaultSpent * data.rate;
        String result = String.format("%.2f %s", converted, data.symbol);

        // Устанавливаем данные в View
        textViewItemName.setText(name);
        textViewItemPrice.setText(result);
        checkBox.setChecked(isChecked);

        // Устанавливаем цвет текста в зависимости от состояния CheckBox
        int textColor = isChecked ? Color.GREEN : Color.RED;
        textViewItemName.setTextColor(textColor);
        textViewItemPrice.setTextColor(textColor);

        if (position == selectedPosition) {
            buttonEdit.setVisibility(View.VISIBLE);
            buttonDelete.setVisibility(View.VISIBLE);
        } else {
            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }

        // Обработка клика по элементу
        convertView.setOnClickListener(v -> {
            selectedPosition = (selectedPosition == position) ? -1 : position;
            notifyDataSetChanged(); // обновить список
        });

        // Обработчики для кнопок
        buttonEdit.setOnClickListener(v -> {
            // Получаем текущий элемент
            String itemData = items.get(position);
            final int[] selectedCategoryId = {0};
            String[] parts = itemData.split("-"); // Разделяем строку
            String itemName = parts[0]; // "Coffee"
            String spentString = parts[1].replaceAll("[^\\d.]", "");

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

            // Создаем EditText для суммы
            EditText inputSpent = new EditText(context);
            inputSpent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputSpent.setText(String.format(Locale.US, "%.2f", (double) DefaultSpent));
            inputSpent.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputSpent.setBackgroundResource(R.drawable.edit_text_style_orange);

            // Ограничиваем ввод только цифрами и одной точкой
            inputSpent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    String text = charSequence.toString();
                    if (text.contains(".")) {
                        // Разрешаем только одну точку
                        if (text.indexOf(".", text.indexOf(".") + 1) != -1) {
                            inputSpent.setText(text.substring(0, text.lastIndexOf(".")));
                            inputSpent.setSelection(inputSpent.getText().length()); // Устанавливаем курсор в конец
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });

            // Применяем LayoutParams
            LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spentParams.setMargins(0, 10, 0, 20);
            inputSpent.setLayoutParams(spentParams); // Применяем параметры


            // Инициализация Spinner для выбора валюты
            Spinner currencySpinner = new Spinner(context);
            String[] currencies = {"֏", "$", "₽"};
            ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
            currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currencySpinner.setAdapter(currencyAdapter);
            currencySpinner.setBackgroundResource(R.drawable.spinner_bg);

            LinearLayout.LayoutParams currencyParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            currencyParams.setMargins(0, 10, 0, 20); // сверху 10, снизу 20
            currencySpinner.setPadding(20, 40, 40, 40);
            currencySpinner.setLayoutParams(currencyParams);


            // Получаем текущую валюту из базы данных
            String currentCurrencySymbol = databaseIncome.getCurs(); // Это возвращает символ валюты (например, "dram", "dollar", "rubli")

            // Преобразуем символ в валютный знак и выбираем в Spinner
            String selectedSymbol = currentCurrencySymbol; // Получаем символ текущей валюты
            int defaultCurrencyPosition = 0; // Изначально установим на 0 (например, драм)

            // Определяем валюту по символу
            switch (selectedSymbol) {
                case "dollar":
                    selectedSymbol = "$";
                    defaultCurrencyPosition = 1;
                    break;
                case "rubli":
                    selectedSymbol = "₽";
                    defaultCurrencyPosition = 2;
                    break;
                case "dram":
                default:
                    selectedSymbol = "֏";
                    defaultCurrencyPosition = 0;
                    break;
            }

// Устанавливаем выбранную валюту в Spinner
            currencySpinner.setSelection(defaultCurrencyPosition); // Устанавливаем валюту по умолчанию

// Обработчик выбора валюты
            final String[] selectedCurrency = {selectedSymbol}; // Храним выбранную валюту
            currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCurrency[0] = currencies[position];
                    double rate = CursHelper.getCursData(selectedCurrency[0]).rate;
                    double value = DefaultSpent * rate; // Конвертируем в выбранную валюту
                    inputSpent.setText(String.format(Locale.US, "%.2f", value)); // Устанавливаем в EditText значение
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });



            FileHelper fileHelper = new FileHelper(context);
            List<String> categories = fileHelper.readCategoriesFromFile(); // Чтение категорий
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


            // Устанавливаем дефолтную категорию
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            int offset = ((MainActivity) context).getoffset();
            int category_id = databaseHelper.getCategoryId(itemName, currentDay , offset);
            List<String> list = fileHelper.readCategoriesFromFile();
            categorySpinner.setSelection(categories.indexOf(list.get(category_id)));

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

            // Контейнер для `EditText`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(categorySpinner);
            layout.addView(currencySpinner);

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                String newspentString = inputSpent.getText().toString().replaceAll("[^\\d.]", ""); // Убираем все, кроме цифр и точки
                double newitemSpent = 0;
                try {
                    newitemSpent = Double.parseDouble(newspentString);  // Преобразуем строку в double
                } catch (NumberFormatException e) {
                    // Обработка ошибки, если ввод некорректен
                    e.printStackTrace();
                }
                double rate = CursHelper.getCursData(selectedCurrency[0]).rate;
                double valueInX = newitemSpent / rate;
                valueInX = Math.round(valueInX * 100.0) / 100.0;


                // Обновляем запись в базе данных
                int currentMonthOffset = ((MainActivity) context).getoffset();
                databaseHelper.updateData(itemName, currentDay, newName, valueInX, currentMonthOffset , selectedCategoryId[0]);

                // Обновляем данные в списке и уведомляем адаптер
                String end = "-false";
                if (checkBox.isChecked()) {
                    end = "-true";
                    databaseIncome.addIncome(DefaultSpent);
                    databaseIncome.addSpent(valueInX);
                }
                CursData dataa = CursHelper.getCursData(databaseIncome.getCurs());
                items.set(position, newName + "-" + valueInX + dataa.symbol + end);
                textViewItemName.setText(newName);
                textViewItemPrice.setText(valueInX * dataa.rate + dataa.symbol);

                notifyDataSetChanged();

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            // Кнопка "Отмена"
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_edit); // Устанавливаем фон
            dialog.show();

        });

        buttonDelete.setOnClickListener(v -> {
            // Создаем AlertDialog для подтверждения удаления
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF0000'>Вы уверены, что хотите удалить элемент?</font>"))
                    .setPositiveButton("Да", (dialog, which) -> {

                        // Действие для кнопки "Удалить"
                        String itemData = items.get(position); // Получаем имя элемента для удаления
                        String item = itemData.split("-")[1];

                        if (itemData.split("-")[2].matches("true")) {
                            databaseIncome.addIncome(DefaultSpent);
                        }

                        String itemName = textViewItemName.getText().toString();
                        deleteItem(itemName, currentDay); // Вызываем метод для удаления

                        items.remove(position);
                        notifyDataSetChanged();

                        Toast.makeText(context, "Удаление завершено", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss());

            // Применяем стиль и показываем окно
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            dialog.show();

            // Изменение цвета кнопок
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
        });



        checkBox.setOnClickListener(v -> {
            String spent = textViewItemPrice.getText().toString();
            int day = currentDay;
            boolean isDone = checkBox.isChecked();
            final String[] value = {""};
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            int currentMonthOffset = ((MainActivity) context).getoffset();

            // Создаем AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#1EFF00'>вы хотите изменить статус на выполненое/невыпелноное(пойми по true false)</font>"))
                    .setMessage("Вы уверены, что хотите изменить статус?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        databaseHelper.setDone(name, day, currentMonthOffset, isDone);

                        if (isDone) {
                            value[0] = "true";
                            String count = spent.replace("₽", "");
                            databaseIncome.addSpent(DefaultSpent);
                        } else {
                            value[0] = "false";
                            String count = spent.replace("₽", "");
                            databaseIncome.addIncome(DefaultSpent);
                        }

                        items.set(position, name + "-" + price + "-" + value[0]);
                        notifyDataSetChanged();  // Обновляем список после изменения состояния
                    })
                    .setNegativeButton("Нет", (dialog, which) -> {
                        checkBox.setChecked(isChecked);
                        dialog.dismiss();
                    });

            // Применяем стиль и показываем окно
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            dialog.show();

            // Изменение цвета кнопок
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
        });











        //Анимации
        buttonEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // При нажатии увеличиваем размер и добавляем тень (свечение)
                        v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Возвращаем в исходное состояние
                        v.animate().scaleX(1f).scaleY(1f).translationZ(0f).setDuration(150).start();
                        break;
                }
                return false;
            }
        });



        return convertView;
    }


    // Метод для удаления элемента по имени и дню
    private void deleteItem(String itemName, int day) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        int currentMonthOffset = ((MainActivity) context).getoffset();
        databaseHelper.deleteData(itemName, day , currentMonthOffset);

        items.remove(itemName);
    }
}

