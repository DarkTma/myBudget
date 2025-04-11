package com.example.mybudget1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private List<ExpenseData> expenseList;

    public ExpenseAdapter(Context context, List<ExpenseData> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseData expense = expenseList.get(position);

        holder.name.setText(expense.getName());
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double converted = expense.getAmount() * curs.rate;
        String result = String.format("%.2f %s", converted, curs.symbol);
        holder.amount.setText("Сумма: " + result);
        holder.day.setText("День: " + expense.getDate());

        holder.amount.setTextColor(context.getResources().getColor(R.color.yellow));

    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView type, name, amount, day, category;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type);
            name = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.amount);
            day = itemView.findViewById(R.id.day);
            category = itemView.findViewById(R.id.category);
        }
    }
}
