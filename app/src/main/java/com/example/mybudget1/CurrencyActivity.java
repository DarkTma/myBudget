package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyActivity extends AppCompatActivity {

    private Spinner currencySpinner;
    private Button applyButton;
    private TextView ratesText;
    private ImageButton btnBack;

    private Switch switchReverse;

    private final Map<String, String> currencySymbols = new HashMap<String, String>() {{
        put("dram", "֏");
        put("dollar", "$");
        put("rubli", "₽");
        put("juan", "元");
        put("evro", "€");
        put("jen", "¥");
        put("lari", "₾");
    }};

    // Объявляем курс для каждой валюты
    private static final double XtoDollar = 0.002;
    private static final double XtoRubli = 0.1;
    private static final double XtoJuan = 0.2;
    private static final double XtoJpy = 0.15;
    private static final double XtoEur = 0.0015;
    private static final double XtoLari = 0.0045;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        DatabaseHelper2 databaseIncome = new DatabaseHelper2(this);

        currencySpinner = findViewById(R.id.currencySpinner);
        applyButton = findViewById(R.id.applyButton);
        ratesText = findViewById(R.id.ratesText);
        btnBack = findViewById(R.id.buttonBackFromCurs);
        switchReverse = findViewById(R.id.switchReverse);
        TextView deftext = findViewById(R.id.deftext);
        deftext.setText("основная валута: " + currencySymbols.get(databaseIncome.getDefaultCurrency()));


        btnBack.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(CurrencyActivity.this, StartActivity.class);
            startActivity(intentGoBack);
            this.finish();
        });

        switchReverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String selectedItem = currencySpinner.getSelectedItem().toString();
            String selectedSymbol = selectedItem.split(" - ")[0];
            String selectedCurrency = getCurrencyBySymbol(selectedSymbol);
            updateCurrencyList(selectedCurrency);
        });

        // Создаём список строк вида "$ - Доллар"
        List<String> currencyDisplayList = new ArrayList<>();
        for (Map.Entry<String, String> entry : currencySymbols.entrySet()) {
            String name = entry.getKey();
            String symbol = entry.getValue();

            // Преобразуем код валюты в удобочитаемое название
            String displayName = "";
            switch (name) {
                case "dollar": displayName = "Доллар"; break;
                case "rubli": displayName = "Рубли"; break;
                case "dram": displayName = "Драм"; break;
                case "juan": displayName = "Юань"; break;
                case "evro": displayName = "Евро"; break;
                case "jen": displayName = "Йен"; break;
                case "lari": displayName = "Лари"; break;
                default: displayName = name; break;
            }

            currencyDisplayList.add(symbol + " - " + displayName);
        }

// Устанавливаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                currencyDisplayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);


        applyButton.setOnClickListener(v -> {
            String selectedItem = currencySpinner.getSelectedItem().toString();

            // Разделяем строку "€ - Евро" и берём только символ
            String selectedSymbol = selectedItem.split(" - ")[0];

            // Получаем код валюты по символу
            String selectedCurrency = getCurrencyBySymbol(selectedSymbol);

            // Подтверждение выбора валюты
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


        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        String currentSymbol = curs.symbol;

        int position = adapter.getPosition(currentSymbol);
        if (position >= 0) {
            currencySpinner.setSelection(position);
        }

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirst = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }

                String selectedItem = currencySpinner.getSelectedItem().toString();

                // Разделяем строку "$ - Доллар" по " - " и берём символ
                String selectedSymbol = selectedItem.split(" - ")[0];

                // Получаем код валюты по символу
                String selectedCurrency = getCurrencyBySymbol(selectedSymbol);

                updateCurrencyList(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        updateCurrencyList(databaseIncome.getCurs());
    }

    private String getCurrencyBySymbol(String symbol) {
        for (Map.Entry<String, String> entry : currencySymbols.entrySet()) {
            if (entry.getValue().equals(symbol)) {
                return entry.getKey();
            }
        }
        return "dram";
    }

    private double getExchangeRate(String baseCurrency, String targetCurrency) {
        // Этот метод возвращает курс обмена для целевой валюты
        switch (baseCurrency) {
            case "dram":
                return getExchangeRateFromBaseToTarget("dram", targetCurrency);
            case "dollar":
                return getExchangeRateFromBaseToTarget("dollar", targetCurrency);
            case "rubli":
                return getExchangeRateFromBaseToTarget("rubli", targetCurrency);
            default:
                return 1.0;
        }
    }

    private double getExchangeRateFromBaseToTarget(String baseCurrency, String targetCurrency) {
        switch (baseCurrency) {
            case "dram":
                switch (targetCurrency) {
                    case "dollar":
                        return XtoDollar;
                    case "rubli":
                        return XtoRubli;
                    case "juan":
                        return XtoJuan;
                    case "evro":
                        return XtoEur;
                    case "jen":
                        return XtoJpy;
                    case "lari":
                        return XtoLari;
                    default:
                        return 1.0;
                }
            case "dollar":
                switch (targetCurrency) {
                    case "dram":
                        return 1.0 / XtoDollar;
                    case "rubli":
                        return 1.0 / XtoRubli;
                    case "juan":
                        return 1.0 / XtoJuan;
                    case "evro":
                        return 1.0 / XtoEur;
                    case "jen":
                        return 1.0 / XtoJpy;
                    case "lari":
                        return 1.0 / XtoLari;
                    default:
                        return 1.0;
                }
            case "rubli":
                switch (targetCurrency) {
                    case "dram":
                        return 1.0 / XtoRubli;
                    case "dollar":
                        return 1.0 / XtoDollar;
                    case "juan":
                        return 1.0 / XtoJuan;
                    case "eur":
                        return 1.0 / XtoEur;
                    case "jen":
                        return 1.0 / XtoJpy;
                    case "lari":
                        return 1.0 / XtoLari;
                    default:
                        return 1.0;
                }
            default:
                return 1.0;
        }
    }

    private void updateCurrencyList(String selectedCurrency) {
        String result = "";

        double baseToDollar = CursHelper.getToDollar();
        double baseToRub = CursHelper.getToRub();
        double baseToDram = CursHelper.getToDram();
        double baseToJuan = CursHelper.getToJuan();
        double baseToEur = CursHelper.getToEur();
        double baseToJen = CursHelper.getToJen();
        double baseToGel = CursHelper.getToLari();

        boolean reverse = switchReverse.isChecked();

        switch (selectedCurrency) {
            case "dollar":
            if (!reverse) {
                result = String.format(
                        "1 $ = %.2f ֏\n1 $ = %.2f ₽\n1 $ = %.2f ¥ (元)\n1 $ = %.2f €\n1 $ = %.2f ¥ (円)\n1 $ = %.2f ₾",
                        baseToDram / baseToDollar,
                        baseToRub / baseToDollar,
                        baseToJuan / baseToDollar,
                        baseToEur / baseToDollar,
                        baseToJen / baseToDollar,
                        baseToGel / baseToDollar
                );
            } else {
                result = String.format(
                        "1 ֏ = %.4f $\n1 ₽ = %.4f $\n1 元 = %.4f $\n1 € = %.4f $\n1 ¥ (円) = %.4f $\n1 ₾ = %.4f $",
                        baseToDollar / baseToDram,
                        baseToDollar / baseToRub,
                        baseToDollar / baseToJuan,
                        baseToDollar / baseToEur,
                        baseToDollar / baseToJen,
                        baseToDollar / baseToGel
                );
            }
            break;


            case "rubli":
                if (!reverse) {
                    result = String.format(
                            "1 ₽ = %.2f ֏\n1 ₽ = %.2f $\n1 ₽ = %.2f ¥ (元)\n1 ₽ = %.2f €\n1 ₽ = %.2f ¥ (円)\n1 ₽ = %.2f ₾",
                            baseToDram / baseToRub, baseToDollar / baseToRub, baseToJuan / baseToRub,
                            baseToEur / baseToRub, baseToJen / baseToRub, baseToGel / baseToRub
                    );
                } else {
                    result = String.format(
                            "1 ֏ = %.4f ₽\n1 $ = %.4f ₽\n1 元 = %.4f ₽\n1 € = %.4f ₽\n1 ¥ (円) = %.4f ₽\n1 ₾ = %.4f ₽",
                            baseToRub / baseToDram, baseToRub / baseToDollar, baseToRub / baseToJuan,
                            baseToRub / baseToEur, baseToRub / baseToJen, baseToRub / baseToGel
                    );
                }
                break;

            case "dram":
                if (!reverse) {
                    result = String.format(
                            "1 ֏ = %.4f $\n1 ֏ = %.4f ₽\n1 ֏ = %.4f ¥ (元)\n1 ֏ = %.4f €\n1 ֏ = %.4f ¥ (円)\n1 ֏ = %.4f ₾",
                            baseToDollar / baseToDram, baseToRub / baseToDram, baseToJuan / baseToDram,
                            baseToEur / baseToDram, baseToJen / baseToDram, baseToGel / baseToDram
                    );
                } else {
                    result = String.format(
                            "1 $ = %.2f ֏\n1 ₽ = %.2f ֏\n1 元 = %.2f ֏\n1 € = %.2f ֏\n1 ¥ (円) = %.2f ֏\n1 ₾ = %.2f ֏",
                            baseToDram / baseToDollar, baseToDram / baseToRub, baseToDram / baseToJuan,
                            baseToDram / baseToEur, baseToDram / baseToJen, baseToDram / baseToGel
                    );
                }
                break;

            case "juan":
                if (!reverse) {
                    result = String.format(
                            "1 元 = %.2f ֏\n1 元 = %.2f $\n1 元 = %.2f ₽\n1 元 = %.2f €\n1 元 = %.2f ¥ (円)\n1 元 = %.2f ₾",
                            baseToDram / baseToJuan, baseToDollar / baseToJuan, baseToRub / baseToJuan,
                            baseToEur / baseToJuan, baseToJen / baseToJuan, baseToGel / baseToJuan
                    );
                } else {
                    result = String.format(
                            "1 ֏ = %.4f 元\n1 $ = %.4f 元\n1 ₽ = %.4f 元\n1 € = %.4f 元\n1 ¥ (円) = %.4f 元\n1 ₾ = %.4f 元",
                            baseToJuan / baseToDram, baseToJuan / baseToDollar, baseToJuan / baseToRub,
                            baseToJuan / baseToEur, baseToJuan / baseToJen, baseToJuan / baseToGel
                    );
                }
                break;

            case "evro":
                if (!reverse) {
                    result = String.format(
                            "1 € = %.2f ֏\n1 € = %.2f $\n1 € = %.2f ₽\n1 € = %.2f 元\n1 € = %.2f ¥ (円)\n1 € = %.2f ₾",
                            baseToDram / baseToEur, baseToDollar / baseToEur, baseToRub / baseToEur,
                            baseToJuan / baseToEur, baseToJen / baseToEur, baseToGel / baseToEur
                    );
                } else {
                    result = String.format(
                            "1 ֏ = %.4f €\n1 $ = %.4f €\n1 ₽ = %.4f €\n1 元 = %.4f €\n1 ¥ (円) = %.4f €\n1 ₾ = %.4f €",
                            baseToEur / baseToDram, baseToEur / baseToDollar, baseToEur / baseToRub,
                            baseToEur / baseToJuan, baseToEur / baseToJen, baseToEur / baseToGel
                    );
                }
                break;

            case "jen":
                if (!reverse) {
                    result = String.format(
                            "1 ¥ (円) = %.2f ֏\n1 ¥ (円) = %.2f $\n1 ¥ (円) = %.2f ₽\n1 ¥ (円) = %.2f 元\n1 ¥ (円) = %.2f €\n1 ¥ (円) = %.2f ₾",
                            baseToDram / baseToJen, baseToDollar / baseToJen, baseToRub / baseToJen,
                            baseToJuan / baseToJen, baseToEur / baseToJen, baseToGel / baseToJen
                    );
                } else {
                    result = String.format(
                            "1 ֏ = %.4f ¥ (円)\n1 $ = %.4f ¥ (円)\n1 ₽ = %.4f ¥ (円)\n1 元 = %.4f ¥ (円)\n1 € = %.4f ¥ (円)\n1 ₾ = %.4f ¥ (円)",
                            baseToJen / baseToDram, baseToJen / baseToDollar, baseToJen / baseToRub,
                            baseToJen / baseToJuan, baseToJen / baseToEur, baseToJen / baseToGel
                    );
                }
                break;

            case "lari":
                if (!reverse) {
                    result = String.format(
                            "1 ₾ = %.2f ֏\n1 ₾ = %.2f $\n1 ₾ = %.2f ₽\n1 ₾ = %.2f 元\n1 ₾ = %.2f €\n1 ₾ = %.2f ¥ (円)",
                            baseToDram / baseToGel, baseToDollar / baseToGel, baseToRub / baseToGel,
                            baseToJuan / baseToGel, baseToEur / baseToGel, baseToJen / baseToGel
                    );
                } else {
                    result = String.format(
                            "1 ֏ = %.4f ₾\n1 $ = %.4f ₾\n1 ₽ = %.4f ₾\n1 元 = %.4f ₾\n1 € = %.4f ₾\n1 ¥ (円) = %.4f ₾",
                            baseToGel / baseToDram, baseToGel / baseToDollar, baseToGel / baseToRub,
                            baseToGel / baseToJuan, baseToGel / baseToEur, baseToGel / baseToJen
                    );
                }
                break;
        }

        ratesText.setText(result);
    }
}

