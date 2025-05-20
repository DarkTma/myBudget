package com.example.mybudget1;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MenuHelper {

    public static void setupMenu(Activity activity) {
        LinearLayout menuLayout = activity.findViewById(R.id.menuLayout);
        View dimLayer = activity.findViewById(R.id.dimLayer);

        Button btnGoalGo = activity.findViewById(R.id.btnGoalGo);
        btnGoalGo.setOnClickListener(v -> {
            goTo(activity, GoalActivity.class);
        });

        ImageButton btnNotif = activity.findViewById(R.id.notifications);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                goTo(activity, ReminderListActivity.class);
            });
        }

        Button buttonHistory = activity.findViewById(R.id.buttonHistory);
        buttonHistory.setOnClickListener(v -> {
            goTo(activity, NotesActivity.class);
        });

        Button lastMonths = activity.findViewById(R.id.btnLastMonths);
        lastMonths.setOnClickListener(v -> {
            goTo(activity, MonthListActivity.class);
        });

        Button monthlySpents = activity.findViewById(R.id.btnMonthlySpents);
        monthlySpents.setOnClickListener(v -> {
            goTo(activity, SpentActivity.class);
        });

        dimLayer.setOnClickListener(v -> {
            menuLayout.setVisibility(View.GONE);
            dimLayer.setVisibility(View.GONE);
        });

        Button btnGrafGo = activity.findViewById(R.id.btnGrafGo);
        btnGrafGo.setOnClickListener(v -> {
            goTo(activity, GraphActivity.class);
        });

        Button btnMaketsGo = activity.findViewById(R.id.btnMaketGo);
        btnMaketsGo.setOnClickListener(v -> {
            goTo(activity, MaketListActivity.class);
        });

        Button btnIncomeActivityGo = activity.findViewById(R.id.btnincomeData);
        btnIncomeActivityGo.setOnClickListener(v -> {
            goTo(activity, IncomeActivity.class);
        });

        Button btnCategoriesGo = activity.findViewById(R.id.btnCategories);
        btnCategoriesGo.setOnClickListener(v -> {
            goTo(activity, CategoriesActivity.class);
        });

        Button btnCursGo = activity.findViewById(R.id.btnCurs);
        btnCursGo.setOnClickListener(v -> {
            goTo(activity, CurrencyActivity.class);
        });

        Button geminiAnalizbtn = activity.findViewById(R.id.btnGeminiGo);
        geminiAnalizbtn.setOnClickListener(v -> {
            goTo(activity, GeminiChatActivity.class);
        });

        Button btnConf = activity.findViewById(R.id.btnConf);
        btnConf.setOnClickListener(v -> {
            goTo(activity, PrivacyPolicyActivity.class);
        });
    }

    private static void goTo(Activity activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
        activity.finish();
    }
}
