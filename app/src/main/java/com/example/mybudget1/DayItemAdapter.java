package com.example.mybudget1;

import static androidx.core.app.PendingIntentCompat.getActivity;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView textViewItem = convertView.findViewById(R.id.textViewItem);
        Button buttonEdit = convertView.findViewById(R.id.buttonEdit);
        Button buttonDelete = convertView.findViewById(R.id.buttonDelete);

        // Set the item text
        textViewItem.setText(items.get(position));

        // Обработчики для кнопок
        buttonEdit.setOnClickListener(v -> {
            // Получаем текущий элемент
            String itemData = items.get(position);
            String[] parts = itemData.split(" - "); // Разделяем строку
            String itemName = parts[0]; // "Coffee"
            String spentString = parts[1].replace("₽", ""); // "100"

            int itemSpent = Integer.parseInt(spentString); // Преобразуем строку в число

            // Создаем всплывающее окно
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Редактирование");

            // Создаем `EditText` для имени
            EditText inputName = new EditText(context);
            inputName.setInputType(InputType.TYPE_CLASS_TEXT);
            inputName.setText(itemName);

            // Создаем `EditText` для суммы
            EditText inputSpent = new EditText(context);
            inputSpent.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputSpent.setText(String.valueOf(itemSpent));

            // Контейнер для `EditText`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);

            builder.setView(layout);

            // Кнопка "Сохранить"
            builder.setPositiveButton("Сохранить", (dialog, which) -> {
                String newName = inputName.getText().toString();
                int newSpent = Integer.parseInt(inputSpent.getText().toString());

                // Обновляем запись в базе данных
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.updateData(itemName, currentDay, newName, newSpent);

                // Обновляем данные в списке и уведомляем адаптер
                items.set(position, newName + " -  " + newSpent + "₽");
                notifyDataSetChanged();

                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show();
            });

            // Кнопка "Отмена"
            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            // Показываем диалог
            builder.show();
        });

        buttonDelete.setOnClickListener(v -> {
            // Действие для кнопки "Удалить"
            String itemData = items.get(position); // Получаем имя элемента для удаления
            String itemName = itemData.split(" ")[0];
            deleteItem(itemName, currentDay); // Вызываем метод для удаления

            Toast.makeText(context, "Удаление завершено. Обновите страницу", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }


    // Метод для удаления элемента по имени и дню
    private void deleteItem(String itemName, int day) {
        // Здесь ты можешь использовать метод deleteDataByName из DatabaseHelper,
        // передав и день, чтобы удалить нужный элемент.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.deleteData(itemName, day); // Новый метод для удаления по имени и дню

        items.remove(itemName); // Удаляем элемент из списка по имени
    }
}

