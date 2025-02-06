package com.example.mybudget1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder> {

    private Context context;
    private List<IncomeItem> incomeList;
    private OnIncomeItemClickListener listener;

    public interface OnIncomeItemClickListener {
        void onEditClick(IncomeItem item);
        void onDeleteClick(IncomeItem item);
    }

    public IncomeAdapter(Context context, List<IncomeItem> incomeList, OnIncomeItemClickListener listener) {
        this.context = context;
        this.incomeList = incomeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_incone, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        IncomeItem incomeItem = incomeList.get(position);
        holder.nameTextView.setText(incomeItem.getName());
        holder.amountTextView.setText(String.valueOf(incomeItem.getAmount()));

        holder.editButton.setOnClickListener(v -> listener.onEditClick(incomeItem));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(incomeItem));
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public static class IncomeViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, amountTextView;
        Button editButton, deleteButton;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.income_name);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
