package com.example.mybudget1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mybudget1.IncomeItem;
import com.example.mybudget1.R;

import java.util.List;

public class IncomeAdapter extends BaseAdapter {
    private Context context;
    private List<IncomeItem> incomeList;

    public IncomeAdapter(Context context, List<IncomeItem> incomeList) {
        this.context = context;
        this.incomeList = incomeList;
    }

    @Override
    public int getCount() {
        return incomeList.size();
    }

    @Override
    public Object getItem(int position) {
        return incomeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.income_item, parent, false);
        }

        IncomeItem income = incomeList.get(position);

        TextView tvName = convertView.findViewById(R.id.tvIncomeName);
        TextView tvAmount = convertView.findViewById(R.id.tvIncomeAmount);
        TextView tvDate = convertView.findViewById(R.id.tvIncomeDate);

        tvName.setText(income.getName());
        tvAmount.setText("Сумма: " + income.getAmount() + " AMD");
        tvDate.setText("Дата: " + income.getDate());

        return convertView;
    }
}
