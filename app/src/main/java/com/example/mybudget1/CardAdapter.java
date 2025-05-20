package com.example.mybudget1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CardItem item);
    }

    private final List<CardItem> items;
    private final OnItemClickListener listener;

    public CardAdapter(List<CardItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text;

        public CardViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.cardIcon);
            text = itemView.findViewById(R.id.cardText);
        }

        public void bind(CardItem item, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = items.get(position);
        holder.icon.setImageResource(item.getIconResId());
        holder.text.setText(item.getText());
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}


