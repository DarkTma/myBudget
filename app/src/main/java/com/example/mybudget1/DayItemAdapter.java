package com.example.mybudget1;

import static androidx.core.app.PendingIntentCompat.getActivity;
import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.List;

public class DayItemAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;
    private int currentDay; // Добавляем текущий день, для которого отображаются элементы
    private Context activity;

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

        // Устанавливаем данные в View
        textViewItemName.setText(name);
        textViewItemPrice.setText(price);
        checkBox.setChecked(isChecked);

        // Устанавливаем цвет текста в зависимости от состояния CheckBox
        int textColor = isChecked ? Color.GREEN : Color.RED;
        textViewItemName.setTextColor(textColor);
        textViewItemPrice.setTextColor(textColor);

        // Обработчики для кнопок
        buttonEdit.setOnClickListener(v -> {
            // Получаем текущий элемент
            String itemData = items.get(position);
            final int[] selectedCategoryId = {0};
            String[] parts = itemData.split("-"); // Разделяем строку
            String itemName = parts[0]; // "Coffee"
            String spentString = parts[1].replace("₽", ""); // "100"

            int itemSpent = Integer.parseInt(spentString); // Преобразуем строку в число

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
            inputSpent.setText(String.valueOf(itemSpent));
            inputSpent.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputSpent.setBackgroundResource(R.drawable.edit_text_style_orange);

            LinearLayout.LayoutParams spentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            spentParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputSpent.setLayoutParams(nameParams);

            FileHelper fileHelper = new FileHelper(context);
            List<String> categories = fileHelper.readCategoriesFromFile(); // Чтение категорий
            Spinner categorySpinner = new Spinner(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);

            // Устанавливаем дефолтную категорию
            categorySpinner.setSelection(categories.indexOf("other"));

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

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                int newSpent = Integer.parseInt(inputSpent.getText().toString());

                // Обновляем запись в базе данных
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
                int currentMonthOffset = ((MainActivity) context).getoffset();
                databaseHelper.updateData(itemName, currentDay, newName, newSpent , currentMonthOffset , selectedCategoryId[0]);

                // Обновляем данные в списке и уведомляем адаптер
                String end = "-false";
                if (checkBox.isChecked()){
                    end = "-true";
                    databaseIncome.addIncome(itemSpent);
                    databaseIncome.addSpent(newSpent);
                }
                items.set(position, newName + "-" + newSpent + "₽"  + end);
                textViewItemName.setText(newName);
                textViewItemPrice.setText(newSpent + "₽");
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
            DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
            // Действие для кнопки "Удалить"
            String itemData = items.get(position); // Получаем имя элемента для удаления
            String item = itemData.split("-")[1];
            int itemCount = Integer.parseInt(item.replace("₽", ""));

            if (itemData.split("-")[2].matches("true")) {
                databaseIncome.addIncome(itemCount);
            }

            String itemName = textViewItemName.getText().toString();
            deleteItem(itemName, currentDay); // Вызываем метод для удаления

            items.remove(position);
            notifyDataSetChanged();

            Toast.makeText(context, "Удаление завершено", Toast.LENGTH_SHORT).show();
        });


        checkBox.setOnClickListener(v -> {
            String spent = textViewItemPrice.getText().toString();
            int day = currentDay;
            boolean isDone = checkBox.isChecked();
            String value;
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
            int currentMonthOffset = ((MainActivity) context).getoffset();
            databaseHelper.setDone(name,day,currentMonthOffset,isDone);
            if (isDone){
                value = "true";
                String count = spent.replace("₽", "");
                databaseIncome.addSpent(Integer.parseInt(count));
            }else {
                value = "false";
                String count = spent.replace("₽", "");
                databaseIncome.addIncome(Integer.parseInt(count));
            }
            items.set(position, name + "-" + price + "-" + value);
            notifyDataSetChanged();  // Обновляем список после изменения состояния
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

