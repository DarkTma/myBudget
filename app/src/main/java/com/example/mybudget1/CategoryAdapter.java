package com.example.mybudget1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    private Context context;
    private List<CategoryItem> categories;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(int categoryId);
        void onDelete(int categoryId);
    }

    public CategoryAdapter(Context context, List<CategoryItem> categories, OnCategoryActionListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categories.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvCategoryName);
            holder.tvPrice = convertView.findViewById(R.id.tvCategoryPrice);
            holder.btnInfo = convertView.findViewById(R.id.btnInfo);
            holder.btnEdit = convertView.findViewById(R.id.btnEdit);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoryItem category = categories.get(position);
        holder.tvName.setText(category.getName());
        holder.tvPrice.setText(String.valueOf(category.getPrice()));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(category.getId()));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category.getId()));

        if ("other".equals(category.getName())) {
            holder.btnEdit.setAlpha(0.0f);  // Сделаем кнопку "Изменить" полупрозрачной
            holder.btnDelete.setAlpha(0.0f); // Сделаем кнопку "Удалить" полупрозрачной

            // Блокируем возможность клика
            holder.btnEdit.setClickable(false);
            holder.btnDelete.setClickable(false);
        } else {
            holder.btnEdit.setAlpha(1f);
            holder.btnDelete.setAlpha(1f);

            // Включаем возможность клика
            holder.btnEdit.setClickable(true);
            holder.btnDelete.setClickable(true);
        }

        holder.btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExpensesActivity.class);
            intent.putExtra("category_id", category.getId());
            context.startActivity(intent);
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvPrice;
        ImageButton btnEdit;
        ImageButton btnDelete;
        ImageButton btnInfo;
    }
}

