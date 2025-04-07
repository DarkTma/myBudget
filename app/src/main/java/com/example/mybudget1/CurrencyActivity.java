package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrencyActivity extends AppCompatActivity {

    private Spinner currencySpinner;
    private Button applyButton;
    private TextView ratesText;
    private ImageButton btnBack;

    private final Map<String, String> currencySymbols = new HashMap<String, String>() {{
        put("dram", "֏");
        put("dollar", "$");
        put("rubli", "₽");
    }};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        currencySpinner = findViewById(R.id.currencySpinner);
        applyButton = findViewById(R.id.applyButton);
        ratesText = findViewById(R.id.ratesText);
        btnBack = findViewById(R.id.buttonBackFromCurs);

        btnBack.setOnClickListener(v->{
            Intent intentGoBack = new Intent(CurrencyActivity.this, StartActivity.class);
            startActivity(intentGoBack);
            this.finish();
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(currencySymbols.values()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        applyButton.setOnClickListener(v -> {
            String selectedSymbol = currencySpinner.getSelectedItem().toString();
            String selectedCurrency = getCurrencyBySymbol(selectedSymbol);
            DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

            new AlertDialog.Builder(this)
                    .setTitle("Подтвердите выбор")
                    .setMessage("Вы уверены, что хотите установить валюту по умолчанию на " + selectedSymbol + "?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        databaseIncome.setCurs(selectedCurrency);
                        Toast.makeText(this, "Установлена валюта: " + selectedSymbol, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        showExchangeRates();
    }

    private String getCurrencyBySymbol(String symbol) {
        for (Map.Entry<String, String> entry : currencySymbols.entrySet()) {
            if (entry.getValue().equals(symbol)) {
                return entry.getKey();
            }
        }
        return "dram";
    }

    private void showExchangeRates() {
        double dramToDollar = CursHelper.getDramToDollar();
        double dramToRub = CursHelper.getDramToRub();

        double dollarToRub = dramToDollar / dramToRub;

        String result = String.format(
                "֏: %.2f → $: 1\n֏: %.2f → ₽: 1\n$: %.2f → ₽: 1",
                dramToDollar, dramToRub, dollarToRub
        );

        ratesText.setText(result);
    }


}
