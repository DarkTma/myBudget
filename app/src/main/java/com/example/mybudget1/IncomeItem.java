package com.example.mybudget1;

public class IncomeItem {
    private String name;
    private int amount;
    private int id;

    public IncomeItem(int id, String name, int amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
}

