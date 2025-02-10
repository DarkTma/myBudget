package com.example.mybudget1;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DayFragment extends Fragment {
    private static final String ARG_DAY = "day";
    private static final String ARG_OFFSET = "offset";
    private DatabaseHelper databaseHelper;
    private int day;
    private int offset;

    public static DayFragment newInstance(int day, int offset) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);
        args.putInt(ARG_OFFSET, offset);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);

        // ✅ Проверяем аргументы
        if (getArguments() != null) {
            day = getArguments().getInt(ARG_DAY, 1);
            offset = getArguments().getInt(ARG_OFFSET, 0);
        }

        // ✅ Находим TextView и ListView
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView selectedDayText = view.findViewById(R.id.selectedDayText);
        ListView listView = view.findViewById(R.id.listView);

        if (selectedDayText != null) {
            selectedDayText.setText("День " + day);
        } else {
            Log.e("DayFragment", "selectedDayText is null!");
        }

        databaseHelper = new DatabaseHelper(requireContext());
        List<String> items = getDataForDay();

        if (!items.isEmpty()) {
            int currentDay = getArguments().getInt(ARG_DAY);
            DayItemAdapter adapter = new DayItemAdapter(requireContext(), items, currentDay);
            listView.setAdapter(adapter);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                    Collections.singletonList("У вас пока нет трат"));
            listView.setAdapter(adapter);
        }

        return view;
    }
    private List<String> getDataForDay() {
        List<String> data = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getData(day, offset);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    int spent = cursor.getInt(cursor.getColumnIndexOrThrow("spent"));
                    boolean isDone = false;
                    String isDonestr = cursor.getString(cursor.getColumnIndexOrThrow("isdone"));
                    if (isDonestr.matches("1")) isDone = true;
                    data.add(name + "-" + spent + "₽" + "-" + isDone);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DayFragment", "Ошибка при получении данных: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return data;
    }

}
