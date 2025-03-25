package com.example.mybudget1;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MonthDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MonthDetailAdapter adapter;
    private ImageButton btnback;
    private EditText searchInput;
    private List<MonthDetailData> monthDetailDataList = new ArrayList<>();
    private List<MonthDetailData> filteredList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_detail);

        recyclerView = findViewById(R.id.recyclerViewMonthDetails);
        btnback = findViewById(R.id.buttonBackdetails);
        searchInput = findViewById(R.id.searchInput);

        btnback.setOnClickListener(v -> {
            Intent intentGoBack = new Intent(MonthDetailActivity.this, MonthListActivity.class);
            startActivity(intentGoBack);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        String prevMonthTable = getIntent().getStringExtra("monthName");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        monthDetailDataList = databaseHelper.getMonthDetailData(db, prevMonthTable);
        filteredList.addAll(monthDetailDataList);

        adapter = new MonthDetailAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(monthDetailDataList);
        } else {
            for (MonthDetailData data : monthDetailDataList) {
                if (data.getName().toLowerCase().contains(query.toLowerCase()) || data.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(data);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
