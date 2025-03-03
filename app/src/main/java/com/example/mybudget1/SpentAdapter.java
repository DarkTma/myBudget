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

import java.util.Calendar;
import java.util.List;

public class SpentAdapter extends BaseAdapter {
    private Context context;
    private List<SpentItem> incomeList;
    private boolean isMonthly;

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

        tvName.setText(spent.getName());
        tvAmount.setText("Сумма: " + spent.getAmount() + " ₽");
        tvDate.setText("Дата: " + spent.getDate());

        String a = spent.getName().trim();
        Cursor cursor = databaseIncome.getMonthly(a);

        btnedit.setOnClickListener(view -> {
            String itemName = spent.getName(); // "Coffee"
            int itemIncome = spent.getAmount(); // "100"

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
            inputDay.setText(String.valueOf(spent.getDate()));
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

                databaseIncome.updateMonthlySpent(itemName, day, newName, newIncome);

                databaseIncome.addSpent(itemIncome);
                databaseIncome.addIncome(newIncome);



                // Обновляем данные в списке и уведомляем адаптер
                spent.change(newName , newIncome , String.valueOf(day));
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
                    Calendar calendar = Calendar.getInstance();
                    int today = calendar.get(Calendar.DAY_OF_MONTH);
                    String name = spent.getName();
                    int count = spent.getAmount();
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
}
