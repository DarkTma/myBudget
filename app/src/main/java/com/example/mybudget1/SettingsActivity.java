package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private TextView incomeNow;
    private TextView stats;
    private TextView spents;
    private Button btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.othersettings);

        incomeNow = findViewById(R.id.income);
        stats = findViewById(R.id.stats);
        spents = findViewById(R.id.spents);
        btnBack = findViewById(R.id.backbtn);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });


        DatabaseHelper2 databaseHelperM = new DatabaseHelper2(this);
        DatabaseHelper databaseHelperD = new DatabaseHelper(this);

        incomeNow.setText("ваш доход: " + String.valueOf(databaseHelperM.getIncome()));
        stats.setText("осталось: " + String.valueOf(databaseHelperM.getIncome() - databaseHelperD.checkAllSpents()));
        spents.setText("траты за месяц: " + String.valueOf(databaseHelperD.checkAllSpents()));

    }
}

