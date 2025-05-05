package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class QuickExpenseInputActivity extends AppCompatActivity {

    private EditText etAmount, etName;
    private Button btnSave;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_expense_input);

        etAmount = findViewById(R.id.etAmount);
        etName = findViewById(R.id.etName);
        btnSave = findViewById(R.id.btnSave);


        InputFilter noDashFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (source.charAt(i) == '-') {
                    return "";
                }
            }
            return null;
        };

        etName.setFilters(new InputFilter[] { noDashFilter });

        btnSave.setOnClickListener(v -> {
            String input = etAmount.getText().toString().trim();

            if (!input.isEmpty()) {
                double amount = Double.parseDouble(input);
                String name = etName.getText().toString();
                // Получаем текущую дату и время
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1; // Месяцы с 0
                int year = calendar.get(Calendar.YEAR);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Имя вида "трата-часы:минуты"
                if (name == null || name.isEmpty()) {
                    name = String.format("трата|%02d:%02d", hour, minute);
                }


                // Сохраняем в базу
                DatabaseHelper db = new DatabaseHelper(this);
                DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);
                db.insertData(day ,name, amount, 0, true,0);
                databaseIncome.addSpent(amount);

                Toast.makeText(this, "Трата записана", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
