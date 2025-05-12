package com.example.mybudget1;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayItemAdapter extends ArrayAdapter<DayItem> {
    private Context context;
    private List<DayItem> items;
    private int currentDay; // Добавляем текущий день, для которого отображаются элементы
    private Context activity;
    private int selectedPosition = -1;

    public DayItemAdapter(Context context, List<DayItem> items, int currentDay) {
        super(context, R.layout.list_item , items);
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
        ImageButton buttonNotif = convertView.findViewById(R.id.buttonNotifications);
        LinearLayout buttonContainer = convertView.findViewById(R.id.buttonContainer);
        LinearLayout nameConteiner = convertView.findViewById(R.id.nameConteiner);
        CheckBox checkBox = convertView.findViewById(R.id.isComplete);


        DayItem item = items.get(position);
        String name = item.getName();
        double price = item.getSpent();
        boolean isChecked = item.isDone();
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData data = CursHelper.getCursData(databaseIncome.getCurs());
        double DefaultSpent = price;
        double converted = DefaultSpent * data.rate;
        double finalA = Math.round(converted * 100.0) / 100.0;
        double valueТ = -1 * finalA;
        String result = String.format("%+.2f %s", valueТ, data.symbol);



        textViewItemName.setText(name);
        textViewItemPrice.setText(result);
        checkBox.setChecked(isChecked);

        boolean isIncome = price < 0;
        int textColor = isIncome ? Color.GREEN : Color.RED;
        textViewItemName.setTextColor(textColor);
        textViewItemPrice.setTextColor(textColor);
        if (!isChecked){
            textViewItemName.setTextColor(Color.GRAY);
            textViewItemPrice.setTextColor(Color.GRAY);
        }


        if (position == selectedPosition) {
            buttonContainer.setVisibility(View.VISIBLE);
            nameConteiner.setVisibility(View.GONE);
        } else {
            buttonContainer.setVisibility(View.GONE);
            nameConteiner.setVisibility(View.VISIBLE);
        }

        // Обработка клика по элементу
//        convertView.setOnClickListener(v -> {
//            selectedPosition = (selectedPosition == position) ? -1 : position;
//            notifyDataSetChanged(); // обновить список
//        });


        GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Обычное нажатие
                selectedPosition = (selectedPosition == position) ? -1 : position;
                notifyDataSetChanged();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // Долгое нажатие
                CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
                String message = "Вы хотите создать шаблон: " + name + " - " + converted + curs.symbol + " ?";
                SpannableString spannableMessage = new SpannableString(message);
                spannableMessage.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_cyan)),
                        0, message.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                SpannableString positiveButtonText = new SpannableString("Добавить");
                positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_cyan)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                SpannableString negativeButtonText = new SpannableString("Отмена");
                negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_cyan)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(Html.fromHtml("<font color='#E0E0E0'>Создание шаблона</font>"))
                        .setMessage(spannableMessage)
                        .setPositiveButton(positiveButtonText, (dialog, which) -> {
                            DatabaseHelper databaseHelper = new DatabaseHelper(context);
                            int offset = ((MainActivity) context).getoffset();
                            int category_id = databaseHelper.getCategoryId(name, currentDay , offset);

                            boolean added = databaseHelper.createMaket(0,name,DefaultSpent,category_id);

                            Toast.makeText(context,
                                    added ? "Шаблон добавлен" : "Такой шаблон уже существует",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                        .setNegativeButton(negativeButtonText, null);

                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                dialog.show();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Двойное нажатие
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                int currentMonthOffset = ((MainActivity) context).getoffset();
                String descr = databaseHelper.getDescription(name, currentDay, currentMonthOffset);
                showDescriptionDialog(context, name, currentDay, descr);
                return true;
            }
        });

        convertView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        textViewItemName.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        textViewItemPrice.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });


        buttonEdit.setOnClickListener(v -> {
            final int[] selectedCategoryId = {0};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF5500'>Редактирование</font>"));

            EditText inputName = new EditText(context);
            inputName.setInputType(InputType.TYPE_CLASS_TEXT);
            inputName.setText(name);
            inputName.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputName.setBackgroundResource(R.drawable.edit_text_style_orange);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputName.setLayoutParams(nameParams);

            EditText inputSpent = new EditText(context);
            inputSpent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputSpent.setText(String.format(Locale.US, "%.2f", Math.abs(converted)));
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
            String[] currencies = {"֏", "$", "₽", "元", "€", "¥", "₾"};
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

            switch (selectedSymbol) {
                case "dollar":
                    selectedSymbol = "$";
                    defaultCurrencyPosition = 1;
                    break;
                case "rubli":
                    selectedSymbol = "₽";
                    defaultCurrencyPosition = 2;
                    break;
                case "yuan":
                    selectedSymbol = "元";
                    defaultCurrencyPosition = 3;
                    break;
                case "evro":
                    selectedSymbol = "€";
                    defaultCurrencyPosition = 4;
                    break;
                case "jen":
                    selectedSymbol = "¥";
                    defaultCurrencyPosition = 5;
                    break;
                case "lari":
                    selectedSymbol = "₾";
                    defaultCurrencyPosition = 6;
                    break;
                case "dram":
                default:
                    selectedSymbol = "֏";
                    defaultCurrencyPosition = 0;
                    break;
            }
            currencySpinner.setSelection(defaultCurrencyPosition); // Устанавливаем валюту по умолчанию

            final String[] selectedCurrency = {selectedSymbol}; // Храним выбранную валюту

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(currencySpinner);

            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            FileHelper fileHelper = new FileHelper(context);
            List<String> list = fileHelper.getAllCategories();

            if (price > 0) {
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




                int offset = ((MainActivity) context).getoffset();
                int category_id = databaseHelper.getCategoryId(name, currentDay, offset);
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

                layout.addView(categorySpinner);
            }

            // Контейнер для `EditText`



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

                double finalAmount = 0;

                String selectednewCurrency = currencySpinner.getSelectedItem().toString();

                switch (selectednewCurrency) {
                    case "֏": // Армянский драм
                        finalAmount = newitemSpent / CursHelper.getToDram();
                        break;
                    case "$": // Доллар США
                        finalAmount = newitemSpent / CursHelper.getToDollar();
                        break;
                    case "₽": // Российский рубль
                        finalAmount = newitemSpent / CursHelper.getToRub();
                        break;
                    case "元": // Китайский юань
                        finalAmount = newitemSpent / CursHelper.getToJuan();
                        break;
                    case "€": // Евро
                        finalAmount = newitemSpent / CursHelper.getToEur();
                        break;
                    case "¥": // Японская иена
                        finalAmount = newitemSpent / CursHelper.getToJen();
                        break;
                    case "₾": // Грузинский лари
                        finalAmount = newitemSpent / CursHelper.getToLari();
                        break;
                    default:
                        finalAmount = newitemSpent;
                        break;
                }

                finalAmount = Math.round(finalAmount * 100.0) / 100.0;
                double valueInX = finalAmount;


                // Обновляем запись в базе данных

                int currentMonthOffset = ((MainActivity) context).getoffset();
                int id = fileHelper.getCategoryIdByName(list.get(selectedCategoryId[0]));
                if (price > 0) {
                    databaseHelper.updateData(item.getId(), currentDay, newName, valueInX, currentMonthOffset, id);
                }else {
                    databaseHelper.updateData(item.getId(), currentDay, newName, -valueInX, currentMonthOffset);
                }

                if (checkBox.isChecked()) {
                    if (DefaultSpent > 0) {
                        databaseIncome.addIncome(DefaultSpent);
                        databaseIncome.addSpent(valueInX);

                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        databaseHelper.saveNote(currentDate, "Изменена трата\n" + name + " - " + DefaultSpent + cursd.symbol + "\nна: " + newName + " - " + valueInX + cursd.symbol, "Spent", "edit" );
                    } else {
                        databaseIncome.addIncome(DefaultSpent);
                        databaseIncome.addSpent(-valueInX);
                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        databaseHelper.saveNote(currentDate, "Изменен доход\n" + name + " - " + -1 * DefaultSpent + cursd.symbol + "\nна: " + newName + " - " + Math.abs(valueInX) + cursd.symbol, "Income", "edit" );
                    }
                }

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("day", currentDay);
                intent.putExtra("isexpented", "false");

                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                context.startActivity(intent);

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            // Кнопка "Отмена"
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_edit); // Устанавливаем фон
            dialog.show();

        });

        buttonNotif.setOnClickListener(v -> {
            int currentMonthOffset = ((MainActivity) context).getoffset();
            if (currentMonthOffset != 0) {
                Toast.makeText(context, "Можно установить уведомление только для текущего месяца", Toast.LENGTH_SHORT).show();
                return;
            }

            // Диалог выбора времени
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute1) -> {
                // Установим дату и время для уведомления
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute1);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.DAY_OF_MONTH, currentDay);

                // Проверим, не в прошлом ли
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(context, "Это время уже прошло", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Создаём уникальный requestCode
                int requestCode = (int) System.currentTimeMillis();

                // Планируем уведомление
                AlarmScheduler.scheduleReminder(context, calendar.getTimeInMillis(), name, requestCode);

                // Сохраняем в базу
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.saveReminder(calendar.getTimeInMillis(), name, requestCode);

                Toast.makeText(context, "Уведомление установлено", Toast.LENGTH_SHORT).show();

            }, hour, minute, true);

            timePickerDialog.show();
        });



        buttonDelete.setOnClickListener(v -> {
            // Создаем AlertDialog для подтверждения удаления
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Html.fromHtml("<font color='#FF0000'>Вы уверены, что хотите удалить элемент?</font>"))
                    .setPositiveButton("Да", (dialog, which) -> {


                        if (item.isDone()) {
                            databaseIncome.addIncome(DefaultSpent);
                        }

                        String itemName = textViewItemName.getText().toString();
                        deleteItem(itemName, currentDay); // Вызываем метод для удаления

                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                        databaseHelper.saveNote(currentDate, "удален расход:\n" + itemName + " - " + DefaultSpent + cursd.symbol, "Spent", "delete" );

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
                        boolean x = false;
                        if (isDone) {
                            value[0] = "true";
                            String count = spent.replace("₽", "");
                            databaseIncome.addSpent(DefaultSpent);
                            x = true;
                        } else {
                            value[0] = "false";
                            String count = spent.replace("₽", "");
                            databaseIncome.addIncome(DefaultSpent);
                        }

                        CursData cursd = CursHelper.getCursData(databaseIncome.getDefaultCurrency());
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String currentDate = sdf.format(new Date());
                        String stat = x ? "выполнен" : "невыполнен";
                        databaseHelper.saveNote(currentDate, "Статус расхода:\n" + name + " - " + DefaultSpent + cursd.symbol + "изменен на " + stat, "Spent", "edit" );

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("day", currentDay);
                        intent.putExtra("isexpented", "false");


                        if (!(context instanceof Activity)) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        context.startActivity(intent);
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

    private void showDescriptionDialog(Context context, String name, int day, String currentDescription) {
        // Создание поля для ввода
        EditText editText = new EditText(context);
        editText.setText(currentDescription);
        editText.setHint("Введите описание...");
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setLines(4);
        editText.setGravity(Gravity.TOP | Gravity.START);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});

        // Установка отступов
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        editText.setPadding(padding, padding, padding, padding);

        // Фон layout
        LinearLayout layout = new LinearLayout(context);
        layout.setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_background));
        layout.setPadding(20, 20, 20, 20);
        layout.addView(editText);

        // Создание диалога
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Описание")
                .setView(layout)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.my_cyan));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.my_cyan));

            positiveButton.setOnClickListener(view -> {
                String newDescription = editText.getText().toString().trim();
                if (!newDescription.equals(currentDescription)) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                    int currentMonthOffset = ((MainActivity) context).getoffset();
                    databaseHelper.updateDescription(name, day, newDescription,currentMonthOffset);
                    Toast.makeText(context, "Описание обновлено", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

}

