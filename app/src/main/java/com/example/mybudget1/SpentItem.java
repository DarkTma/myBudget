package com.example.mybudget1;

public class SpentItem {
    private String name;
    private double amount;
    private String date;

    public SpentItem(String name, double amount, String date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {return amount;}

    public String getDate() {
        return date;
    }


    public void change(String newName , double newAmount , String newDate){
        this.name = newName;
        this.amount = newAmount;
        this.date = newDate;
    }

}
