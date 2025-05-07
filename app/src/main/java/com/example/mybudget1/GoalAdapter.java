package com.example.mybudget1;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URI;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goalList;
    private OnGoalClickListener onGoalClickListener;
    private Context context;

    // Конструктор
    public GoalAdapter(Context context ,List<Goal> goalList, OnGoalClickListener onGoalClickListener) {
        this.goalList = goalList;
        this.onGoalClickListener = onGoalClickListener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.nameTextView.setText(goal.getName());
        holder.savedTextView.setText(String.valueOf(goal.getCurrentAmount()));
        holder.amountTextView.setText(String.valueOf(goal.getAmount()));

        double current = goal.getCurrentAmount();
        double target = goal.getAmount();
        int progress = 0;

        if (target > 0) {
            progress = (int) ((current * 100.0f) / target);
        }

        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(progress);
        holder.progressText.setText(progress + "%");

        holder.savedTextView.setText(String.valueOf(current));
        holder.amountTextView.setText(String.valueOf(target));


        // Загрузка изображения с помощью Glide
        Glide.with(holder.itemView)
                .load(new File(goal.getImagePath().replace("file://", "")))
                .placeholder(R.drawable.default_goal)
                .into(holder.imageView);

        Log.d("GoalAdapter", "Image path: " + goal.getImagePath());



        holder.addButton.setOnClickListener(v -> {
            if (onGoalClickListener != null) {
                onGoalClickListener.onAddMoneyClick(holder.getAdapterPosition());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onGoalClickListener != null) {
                onGoalClickListener.onGoalDeleteClick(holder.getAdapterPosition());
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (onGoalClickListener != null) {
                onGoalClickListener.onGoalEditClick(holder.getAdapterPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public interface OnGoalClickListener {
        void onGoalDeleteClick(int position);
        void onAddMoneyClick(int position);
        void onGoalEditClick(int position);
    }

    // ViewHolder для элемента списка
    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, amountTextView, savedTextView, progressText;
        ImageView imageView;
        ImageButton addButton, deleteButton , editButton;

        ProgressBar progressBar;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.goalName);
            amountTextView = itemView.findViewById(R.id.goalTargetAmount);
            savedTextView = itemView.findViewById(R.id.goalCompletedAmount);
            imageView = itemView.findViewById(R.id.goalImage); // <- это твой ImageView
            addButton = itemView.findViewById(R.id.btnAddMoney);
            deleteButton = itemView.findViewById(R.id.btnDeleteGoal);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            editButton = itemView.findViewById(R.id.btnEditGoal);
        }
    }



}


