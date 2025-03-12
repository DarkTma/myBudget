package com.example.mybudget1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MonthData> monthDataList;
    private Context context;

    public MonthAdapter(Context context, List<MonthData> monthDataList) {
        this.context = context;
        this.monthDataList = monthDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Инфлейтим layout для каждого элемента списка
        View view = LayoutInflater.from(context).inflate(R.layout.item_month, parent, false);
        return new RecyclerView.ViewHolder(view) {}; // Возвращаем анонимный ViewHolder
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Получаем данные из списка
        MonthData monthData = monthDataList.get(position);

        // Привязываем данные к элементам интерфейса через itemView
        View itemView = viewHolder.itemView;
        TextView monthName = itemView.findViewById(R.id.monthName);
        TextView income = itemView.findViewById(R.id.income);
        TextView spent = itemView.findViewById(R.id.spent);

        monthName.setText(monthData.getMonthName());
        monthName.setTextColor(Color.WHITE);
        income.setText("Income: " + monthData.getIncome());
        income.setTextColor(Color.GREEN);
        spent.setText("Spent: " + monthData.getSpent());
        spent.setTextColor(Color.YELLOW);

        // Обработчик клика на элемент
        itemView.setOnClickListener(v -> {
            String a = monthData.getMonthName();
            Intent intent = new Intent(context, MonthDetailActivity.class);
            intent.putExtra("monthName", a);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return monthDataList.size();
    }
}
