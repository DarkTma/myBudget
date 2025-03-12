package com.example.mybudget1;

public class MonthDetailData {
    private String type;  // Income или Spent
    private String name;  // Имя категории
    private int amount;   // Сумма (доход или расход)
    private int day;      // День месяца

    public MonthDetailData(String type, String name, int amount, int day) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.day = day;
    }

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public int getDay() {
        return day;
    }
}

