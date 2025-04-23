package com.example.mybudget1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthDetailAdapter extends RecyclerView.Adapter<MonthDetailAdapter.MonthDetailViewHolder> {

    private List<MonthDetailData> monthDetailDataList;
    private Context context; // добавляем поле

    public MonthDetailAdapter(Context context, List<MonthDetailData> monthDetailDataList) {
        this.context = context;
        this.monthDetailDataList = monthDetailDataList;
    }

    @Override
    public MonthDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_detail, parent, false);
        return new MonthDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MonthDetailViewHolder holder, int position) {
        MonthDetailData data = monthDetailDataList.get(position);

        holder.type.setTextColor(Color.WHITE);
        if (data.getType().matches("Income")) {
            holder.type.setText("Доход");
            holder.name.setTextColor(Color.GREEN);
            holder.amount.setTextColor(Color.GREEN);
            holder.day.setTextColor(Color.WHITE);
            holder.category.setVisibility(View.GONE);
        } else if (data.getType().matches("Spent")) {
            holder.type.setText("Рассход");
            holder.name.setTextColor(Color.YELLOW);
            holder.amount.setTextColor(Color.YELLOW);
            holder.day.setTextColor(Color.WHITE);
        } else if (data.getType().matches("MSpent")) {
            holder.type.setText("Ежемесячная трата");
            holder.name.setTextColor(Color.CYAN);
            holder.amount.setTextColor(Color.CYAN);
            holder.day.setTextColor(Color.WHITE);
            holder.category.setVisibility(View.GONE);
        }

        holder.name.setText(data.getName());

        // Вот здесь можно теперь использовать context:
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());

        double finalAmount = Math.round(data.getAmount() * curs.rate * 100.0) / 100.0;

        holder.amount.setText("Сумма: " + finalAmount + " " + curs.symbol);
        holder.day.setText("День: " + data.getDay());
        holder.category.setText("Котегория: " + data.getCategory());

        if (data.getType().matches("Info")) {
            holder.type.setText("Важно");
            holder.name.setText(data.getName());
            holder.name.setTextColor(Color.RED);
            holder.amount.setVisibility(View.GONE);
            holder.day.setVisibility(View.GONE);
            holder.category.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return monthDetailDataList.size();
    }

    public static class MonthDetailViewHolder extends RecyclerView.ViewHolder {
        TextView type, name, amount, day, category;

        public MonthDetailViewHolder(View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type);
            name = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
            day = itemView.findViewById(R.id.day);
            category = itemView.findViewById(R.id.category);
        }
    }
}
