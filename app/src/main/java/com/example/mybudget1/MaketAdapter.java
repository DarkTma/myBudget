package com.example.mybudget1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class MaketAdapter extends RecyclerView.Adapter<MaketAdapter.MaketViewHolder> {

    private List<Maket> maketList;
    private Context context;

    public interface OnMaketActionListener {
        void onCheck(Maket maket);
        void onEdit(Maket maket);
        void onDelete(Maket maket);
    }

    private OnMaketActionListener actionListener;

    public MaketAdapter(Context context, List<Maket> maketList, OnMaketActionListener listener) {
        this.context = context;
        this.maketList = maketList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public MaketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.maket_item, parent, false);
        return new MaketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaketViewHolder holder, int position) {
        Maket maket = maketList.get(position);
        holder.textName.setText(maket.getName());
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData curs = CursHelper.getCursData(databaseIncome.getCurs());
        double x = Math.abs(maket.getAmount());
        double amaunt = Math.round(( x * curs.rate) * 100.0) / 100.0;
        holder.textAmount.setText("сумма: " + amaunt + curs.symbol);

        holder.btnCheck.setOnClickListener(v -> actionListener.onCheck(maket));
        holder.btnEdit.setOnClickListener(v -> actionListener.onEdit(maket));
        holder.btnDelete.setOnClickListener(v -> actionListener.onDelete(maket));
    }

    @Override
    public int getItemCount() {
        return maketList.size();
    }

    public static class MaketViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAmount;
        ImageButton btnCheck, btnEdit, btnDelete;

        public MaketViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textAmount = itemView.findViewById(R.id.textAmount);
            btnCheck = itemView.findViewById(R.id.btnCheck);
            btnEdit = itemView.findViewById(R.id.btnEditM);
            btnDelete = itemView.findViewById(R.id.btnDeleteM);
        }
    }
}

