package com.example.mybudget1;

public class IncomeItem {
    private String name;
    private double amount;
    private int date;
    boolean monthly;
    boolean active;

    public IncomeItem(String name, double amount, int date, boolean monthly) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.monthly = monthly;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {return amount;}

    public int getDate() {
        return date;
    }

    public boolean getMonthly() {return monthly;}

    public boolean getActive() {return active;}

    public void change(String newName , double newAmount , int newDate){
        this.name = newName;
        this.amount = newAmount;
        this.date = newDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
