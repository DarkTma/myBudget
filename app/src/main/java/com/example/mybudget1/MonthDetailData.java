package com.example.mybudget1;

public class MonthDetailData {
    private String type;  // Income или Spent
    private String name;  // Имя категории
    private double amount;   // Сумма (доход или расход)
    private int day;      // День месяца
    private String category;

    public MonthDetailData(String type, String name, double amount, int day , String category) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.day = day;
        this.category = category;
    }

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public int getDay() {
        return day;
    }

    public  String getCategory(){ return category; }
}

