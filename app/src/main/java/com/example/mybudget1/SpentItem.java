package com.example.mybudget1;

public class SpentItem {
    private String name;
    private double amount;
    private int date;
    private boolean active;

    public SpentItem(String name, double amount, int date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {return amount;}

    public int getDate() {
        return date;
    }


    public void change(String newName , double newAmount , int newDate){
        this.name = newName;
        this.amount = newAmount;
        this.date = newDate;
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public boolean getActive(){ return active;}
}
