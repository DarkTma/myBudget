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

        btnAddSpent.setOnClickListener( v -> {
            TextView customTitle = new TextView(this);
            customTitle.setText("Добавить трату");
            customTitle.setTextSize(20);
            customTitle.setTextColor(ContextCompat.getColor(this, R.color.my_green));
            customTitle.setPadding(0, 20, 0, 20);
            customTitle.setGravity(Gravity.CENTER);

            EditText name = new EditText(this);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint("Название траты");
            name.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            name.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            name.setLayoutParams(nameParams);

            EditText spent = new EditText(this);
            spent.setInputType(InputType.TYPE_CLASS_NUMBER);
            spent.setHint("сумма");
            spent.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            spent.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams incomeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            incomeParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            spent.setLayoutParams(nameParams);

            EditText day = new EditText(this);
            day.setInputType(InputType.TYPE_CLASS_NUMBER);
            day.setHint("день траты(по умлч 1 число)");
            day.setPadding(0, 30, 0, 10); // Добавляем больше отступов
            day.setBackgroundResource(R.drawable.edit_text_style);

            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dayParams.setMargins(0, 10, 0, 20); // Устанавливаем отступы
            day.setLayoutParams(nameParams);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(name);
            layout.addView(spent);
            layout.addView(day);

            // Создаём AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            SpannableString positiveButtonText = new SpannableString("Добавить");
            positiveButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, positiveButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString negativeButtonText = new SpannableString("Отмена");
            negativeButtonText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.my_green)), 0, negativeButtonText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setTitle(Html.fromHtml("<font color='#00FF82'>Введите данные</font>"));
            builder.setView(layout)
                    .setPositiveButton(positiveButtonText, (dialog, which) -> {
                        if (Integer.parseInt(spent.getText().toString()) != 0){
                            String nameText = name.getText().toString();
                            if (nameText.equals("")){
                                nameText = "доход";
                            }

                            int incomeText = Integer.parseInt(spent.getText().toString());

                            int dayText;
                            Calendar calendar = Calendar.getInstance();
                            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                            int today = calendar.get(Calendar.DAY_OF_MONTH);

                            if (Integer.parseInt(day.getText().toString()) > daysInMonth){
                                Toast.makeText(this, "данного дня нет в  месяце", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                if (!day.getText().toString().equals("") && !day.getText().toString().matches("0")) {
                                    dayText = Integer.parseInt(day.getText().toString());
                                } else {
                                    dayText = 1;
                                }
                                boolean isdone = false;
                                if (dayText < today){
                                    isdone = true;
                                }

                                databaseIncome.addMonthlySpent(nameText, incomeText, dayText , isdone);

                                if (dayText <= today) {
                                    databaseIncome.addSpent(incomeText);
                                } else {
                                    databaseIncome.setMonthlySpentDone(false, nameText);
                                }

                                dialog.dismiss();

                                Log.e("lolya" , databaseIncome.getMonthlySpentDone(nameText , dayText));

                                Intent intent = new Intent(SpentActivity.this, SpentActivity.class);
                                startActivity(intent);
                            }

                        } else {
                            Toast.makeText(this, "вы недобавили трату", Toast.LENGTH_SHORT).show();
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
                int spentNum = income.getInt(income.getColumnIndexOrThrow("spent"));
                String date = income.getString(income.getColumnIndexOrThrow("spentday"));
                spentList.add(new SpentItem(name, spentNum, date));
            } while (income.moveToNext());
        }

        // Подключаем адаптер
        adapter = new SpentAdapter(this, spentList);
        listViewIncome.setAdapter(adapter);
    }
}

