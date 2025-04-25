package com.example.mybudget1;

class BudgetItem {
    String name;
    int date;
    int amount;
    boolean isIncome;
    boolean selected = true;

    public BudgetItem(String name, int date, int amount, boolean isIncome) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }
}

