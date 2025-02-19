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
        TextView mustDo = convertView.findViewById(R.id.day_mustDo);

        WeekItem currentItem = items.get(position);

        dayName.setText(currentItem.getDayName());
        spent.setText(currentItem.getSpent());
        mustDo.setText(currentItem.getMustDo());

        String spentNum = spent.getText().toString().replaceAll("[^0-9]", ""); // Удаляем все символы, кроме цифр
        String mustDoText = mustDo.getText().toString();
        int spentN = Integer.parseInt(spentNum);
        String mustDoOnlyNum = mustDoText.replaceAll("[^0-9]", ""); // Удаляем все символы, кроме цифр
        int mustDoNum = Integer.parseInt(mustDoOnlyNum);
        double procent = ((double) spentN / mustDoNum) * 100;

        if (mustDoNum == 0 || procent == 100){
            dayName.setTextColor(Color.GREEN);
            spent.setTextColor(Color.GREEN);
            mustDo.setTextColor(Color.GREEN);
        } else if (procent >= 40){
            int color = ContextCompat.getColor(context, R.color.my_orange);
            dayName.setTextColor(color);
            spent.setTextColor(color);
            mustDo.setTextColor(color);
        } else {
            dayName.setTextColor(Color.RED);
            spent.setTextColor(Color.RED);
            mustDo.setTextColor(Color.RED);
        }

        return convertView;
    }
}


class WeekItem {
    private String dayName;
    private String spent;
    private String mustDo;

    public WeekItem(String dayName, String spent, String mustDo) {
        this.dayName = dayName;
        this.spent = spent;
        this.mustDo = mustDo;
    }

    public String getDayName() {
        return dayName;
    }

    public String getMustDo() { return mustDo; }

    public String getSpent() {
        return spent;
    }


}