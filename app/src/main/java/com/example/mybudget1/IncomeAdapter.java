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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.mybudget1.IncomeItem;
import com.example.mybudget1.R;

import java.util.List;

public class IncomeAdapter extends BaseAdapter {
    private Context context;
    private List<IncomeItem> incomeList;
    private boolean isMonthly;

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

        tvName.setText(income.getName());
        tvAmount.setText("Сумма: " + income.getAmount() + " ₽");
        tvDate.setText("Дата: " + income.getDate());

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



        btnedit.setOnClickListener(view -> {
            String itemName = income.getName(); // "Coffee"
            int itemIncome = income.getAmount(); // "100"

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
            inputSpent.setLayoutParams(nameParams);

            //Создаем `EditText` для дня получения
            EditText inputDay = new EditText(context);
            inputDay.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputDay.setText(String.valueOf(income.getDate()));
            inputDay.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            inputDay.setBackgroundResource(R.drawable.edit_text_style_orange);

            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            inputParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            inputDay.setLayoutParams(nameParams);

            // Контейнер для `EditText`
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputName);
            layout.addView(inputSpent);
            layout.addView(inputDay);

            builder.setView(layout);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.my_orange)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Кнопка "Сохранить"
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                String newName = inputName.getText().toString();
                int newIncome = Integer.parseInt(inputSpent.getText().toString());
                int day = Integer.parseInt(inputDay.getText().toString());

                // Обновляем запись в базе данных

                databaseIncome.updateData(itemName, day, newName, newIncome);

                // Обновляем данные в списке и уведомляем адаптер
                income.change(newName , newIncome , String.valueOf(day));
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
            builder.setTitle(Html.fromHtml("<font color='#FF5500'>Внимание!!! </font>"));

            // Текст для описания
            TextView textView = new TextView(context);
            textView.setText("если вы удалите полностю доход то он пропадет полностью, если вы" + "\n" +
                    "просто в будущем не будете его получать, просто уберите галочку и доход" +
                    "\n" + "станет одноразовым, а в конце месяца пропадет.");
            textView.setTextColor(ContextCompat.getColor(context, R.color.my_red));
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
                    int day = Integer.parseInt(income.getDate());
                    databaseIncome.deleteIncome(name, day);  // Удаление дохода из базы данных
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

            checkBox.setOnClickListener( view -> {
            String name = income.getName();
            boolean isMonthlyy = checkBox.isChecked();
            databaseIncome.setMonthly(name , isMonthlyy);
            notifyDataSetChanged();
        });




        return convertView;
    }
}
