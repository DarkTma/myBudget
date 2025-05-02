package com.example.mybudget1;

public class SpentItem {
    private int id;
    private String nextDate;
    private String name;
    private double amount;
    private int date;
    private boolean active;

    public SpentItem(int id,String name, double amount, int date, String nextDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.nextDate = nextDate;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {return amount;}

    public int getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public String getNextDate() {
        return nextDate;
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
