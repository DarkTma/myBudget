package com.example.mybudget1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    private List<MonthData> monthDataList; // Список объектов MonthData
    private Context context;

    public MonthAdapter(Context context, List<MonthData> monthDataList) {
        this.context = context;
        this.monthDataList = monthDataList;
    }

    @Override
    public MonthViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_month, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MonthViewHolder holder, int position) {
        MonthData monthData = monthDataList.get(position);

        holder.monthName.setText(monthData.getMonthName()); // Название месяца
        holder.income.setText("Income: " + monthData.getIncome()); // Доход
        holder.spent.setText("Spent: " + monthData.getSpent()); // Расходы
    }

    @Override
    public int getItemCount() {
        return monthDataList.size(); // Размер списка месяцев
    }

    // ViewHolder для каждого элемента
    public static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView monthName, income, spent;

        public MonthViewHolder(View itemView) {
            super(itemView);
            monthName = itemView.findViewById(R.id.monthName);
            income = itemView.findViewById(R.id.income);
            spent = itemView.findViewById(R.id.spent);
        }
    }
}
