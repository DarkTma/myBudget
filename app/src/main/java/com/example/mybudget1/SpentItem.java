package com.example.mybudget1;

public class SpentItem {
    private String name;
    private int amount;
    private String date;

    public SpentItem(String name, int amount, String date) {
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {return amount;}

    public String getDate() {
        return date;
    }


    public void change(String newName , int newAmount , String newDate){
        this.name = newName;
        this.amount = newAmount;
        this.date = newDate;
    }

}
