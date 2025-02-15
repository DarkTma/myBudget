package com.example.mybudget1;

public class IncomeItem {
    private String name;
    private int amount;
    private String date;

    public IncomeItem(String name, int amount, String date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
