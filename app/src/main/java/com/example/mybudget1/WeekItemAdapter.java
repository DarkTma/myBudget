package com.example.mybudget1;


import android.graphics.Color;
import android.widget.BaseAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class WeekItemAdapter extends BaseAdapter {
    private Context context;
    private List<WeekItem> items; // Список объектов

    public WeekItemAdapter(Context context, List<WeekItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.day_items, parent, false);
        }

        TextView dayName = convertView.findViewById(R.id.dayName);
        TextView spent = convertView.findViewById(R.id.day_spent);
        TextView spentProcent = convertView.findViewById(R.id.day_spent_procent);
        TextView mustDo = convertView.findViewById(R.id.day_mustdo);

        WeekItem currentItem = items.get(position);

        dayName.setText(currentItem.getDayName());
        spent.setText(currentItem.getSpent());
        spentProcent.setText(currentItem.getSpentProcent());
        mustDo.setText(currentItem.getMustDo());

        String text = spentProcent.getText().toString();
        String onlyNumbers = text.replaceAll("[^0-9]", ""); // Удаляем все символы, кроме цифр
        String mustDoText = mustDo.getText().toString();
        String mustDoOnlyNum = mustDoText.replaceAll("[^0-9]", ""); // Удаляем все символы, кроме цифр
        int procent = Integer.parseInt(onlyNumbers);
        int mustDoNum = Integer.parseInt(mustDoOnlyNum);

        if (procent == 100 || mustDoNum == 0){
            dayName.setTextColor(Color.GREEN);
            spent.setTextColor(Color.GREEN);
            spentProcent.setTextColor(Color.GREEN);
        } else if (procent >= 40){
            int color = ContextCompat.getColor(context, R.color.my_orange);
            dayName.setTextColor(color);
            spent.setTextColor(color);
            spentProcent.setTextColor(color);
        } else {
            dayName.setTextColor(Color.RED);
            spent.setTextColor(Color.RED);
            spentProcent.setTextColor(Color.RED);
        }

        return convertView;
    }
}


class WeekItem {
    private String dayName;
    private String spent;
    private String spentPercent;
    private String mustDo;

    public WeekItem(String dayName, String spent, String spentPercent , String mustDo) {
        this.dayName = dayName;
        this.spent = spent;
        this.spentPercent = spentPercent;
        this.mustDo = mustDo;
    }

    public String getDayName() {
        return dayName;
    }

    public String getMustDo() { return mustDo; }

    public String getSpent() {
        return spent;
    }

    public String getSpentProcent() {
        return spentPercent;
    }

}