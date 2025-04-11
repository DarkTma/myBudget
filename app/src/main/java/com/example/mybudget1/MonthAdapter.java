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

        String name = getName(monthData.getMonthName().split("_")[2]) + "  " + monthData.getMonthName().split("_")[1];
        monthName.setText(name);
        monthName.setTextColor(Color.WHITE);
        DatabaseHelper2 databaseIncome = new DatabaseHelper2(context);
        CursData curs  =CursHelper.getCursData(databaseIncome.getCurs());
        income.setText("Доход: " + monthData.getIncome() * curs.rate + " " + curs.symbol);
        income.setTextColor(Color.GREEN);
        spent.setText("Расход: " + monthData.getSpent() * curs.rate + " " + curs.symbol);
        spent.setTextColor(Color.YELLOW);

        // Обработчик клика на элемент
        itemView.setOnClickListener(v -> {
            String a = monthData.getMonthName();
            Intent intent = new Intent(context, MonthDetailActivity.class);
            intent.putExtra("monthName", a);
            context.startActivity(intent);
        });
    }

    private String getName(String s) {
        int id = Integer.parseInt(s);
        String name = "";
        String[] months = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        switch (id) {
            case 1:
                name = months[0]; // Январь
                break;
            case 2:
                name = months[1]; // Февраль
                break;
            case 3:
                name = months[2]; // Март
                break;
            case 4:
                name = months[3]; // Апрель
                break;
            case 5:
                name = months[4]; // Май
                break;
            case 6:
                name = months[5]; // Июнь
                break;
            case 7:
                name = months[6]; // Июль
                break;
            case 8:
                name = months[7]; // Август
                break;
            case 9:
                name = months[8]; // Сентябрь
                break;
            case 10:
                name = months[9]; // Октябрь
                break;
            case 11:
                name = months[10]; // Ноябрь
                break;
            case 12:
                name = months[11]; // Декабрь
                break;
            default:
                name = "Неизвестный месяц"; // Защита от некорректного ввода
        }

        return name;
    }


    @Override
    public int getItemCount() {
        return monthDataList.size();
    }
}
