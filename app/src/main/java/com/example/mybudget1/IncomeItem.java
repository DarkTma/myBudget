package com.example.mybudget1;

public class IncomeItem {
    private String name;
    private double amount;
    private String date;
    boolean monthly;

    public IncomeItem(String name, double amount, String date, boolean monthly) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.monthly = monthly;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {return amount;}

    public String getDate() {
        return date;
    }

    public boolean getMonthly() {return monthly;}

    public void change(String newName , double newAmount , String newDate){
        this.name = newName;
        this.amount = newAmount;
        this.date = newDate;
    }


}
