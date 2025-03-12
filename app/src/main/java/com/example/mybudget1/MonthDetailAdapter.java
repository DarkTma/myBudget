package com.example.mybudget1;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthDetailAdapter extends RecyclerView.Adapter<MonthDetailAdapter.MonthDetailViewHolder> {

    private List<MonthDetailData> monthDetailDataList;

    public MonthDetailAdapter(List<MonthDetailData> monthDetailDataList) {
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
        if (data.getType().matches("Income")){
            holder.type.setText("Доход");
            holder.name.setTextColor(Color.GREEN);
            holder.amount.setTextColor(Color.GREEN);
            holder.day.setTextColor(Color.WHITE);
        } else {
            holder.type.setText("Рассход");
            holder.name.setTextColor(Color.YELLOW);
            holder.amount.setTextColor(Color.YELLOW);
            holder.day.setTextColor(Color.WHITE);
        }
        holder.name.setText(data.getName());
        holder.amount.setText("сумма: " + data.getAmount());
        holder.day.setText("День: " + data.getDay());
    }

    @Override
    public int getItemCount() {
        return monthDetailDataList.size();
    }

    public static class MonthDetailViewHolder extends RecyclerView.ViewHolder {
        TextView type, name, amount, day;

        public MonthDetailViewHolder(View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type);
            name = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
            day = itemView.findViewById(R.id.day);
        }
    }
}

