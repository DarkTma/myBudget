package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class SpentActivity extends AppCompatActivity {

    private ListView listViewIncome;
    private SpentAdapter adapter;
    private Button btnAddSpent;
    private ArrayList<SpentItem> spentList;
    private ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_spents);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        listViewIncome = findViewById(R.id.listViewSpent);
        btnBack = findViewById(R.id.buttonBackFromSpents);
        btnAddSpent = findViewById(R.id.btnAddMonthlySpent);

        btnAddSpent.setOnClickListener(v -> {
            TextView customTitle = new TextView(this);
            customTitle.setText("Добавить трату");
            customTitle.setTextSize(20);
            customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            customTitle.setPadding(0, 20, 0, 20);
            customTitle.setGravity(Gravity.CENTER);

            EditText name = new EditText(this);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint("Название траты");
            name.setPadding(0, 30, 0, 10);
            name.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20);
            name.setLayoutParams(nameParams);

            EditText spent = new EditText(this);
            spent.setInputType(InputType.TYPE_CLASS_NUMBER);
            spent.setHint("Сумма");
            spent.setPadding(0, 30, 0, 10);
            spent.setBackgroundResource(R.drawable.edit_text_style);
            spent.setLayoutParams(nameParams);

            NumberPicker dayPicker = new NumberPicker(this);
            dayPicker.setMinValue(1);
            dayPicker.setMaxValue(31);
            dayPicker.setWrapSelectorWheel(true);

            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dayParams.setMargins(0, 10, 0, 20);
            dayPicker.setLayoutParams(dayParams);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(name);
            layout.addView(spent);
            layout.addView(dayPicker);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"))
                    .setView(layout)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        if (!spent.getText().toString().isEmpty() && Integer.parseInt(spent.getText().toString()) != 0) {
                            String nameText = name.getText().toString().trim();
                            if (nameText.isEmpty()) {
                                nameText = "Трата";
                            }

                            int spentValue = Integer.parseInt(spent.getText().toString());
                            int selectedDay = dayPicker.getValue();

                            Calendar calendar = Calendar.getInstance();
                            int today = calendar.get(Calendar.DAY_OF_MONTH);

                            databaseIncome.addMonthlySpent(nameText, spentValue, selectedDay);
                            dialog.dismiss();

                            Intent intent = new Intent(SpentActivity.this, SpentActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Вы не добавили трату", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(negativeButtonText, null);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); // Устанавливаем фон
            dialog.show();
        });

        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(SpentActivity.this, StartActivity.class);
            startActivity(intentGoBack);
        });


        // Заполняем список тестовыми данными
        spentList = new ArrayList<>();
        Cursor income = databaseIncome.getMonthlySpentList();
        if (income != null && income.moveToFirst()) {
            do {
                String name = income.getString(income.getColumnIndexOrThrow("name"));
                int spentNum = income.getInt(income.getColumnIndexOrThrow("monthly_spent"));
                String date = income.getString(income.getColumnIndexOrThrow("spentday"));
                spentList.add(new SpentItem(name, spentNum, date));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new SpentAdapter(this, spentList);
        listViewIncome.setAdapter(adapter);
    }
}

