package com.example.mybudget1;

class BudgetItem {
    String name;
    int date;
    int amount;
    boolean isIncome;
    boolean selected = true;
    int category_id;

    public BudgetItem(String name, int date, int amount, boolean isIncome) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    public BudgetItem(String name, int date, int amount, boolean isIncome, int category_id) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
        this.category_id = category_id;
    }
}

