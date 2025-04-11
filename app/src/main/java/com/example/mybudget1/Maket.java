package com.example.mybudget1;

public class Maket {
    private int id;
    private String name;
    private String type;
    private double amount;
    private int category_id;

    public Maket(int id, String name, String type, double amount , int category_id) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.category_id = category_id;
    }

    public int getId() { return id; }
    public int getCategory_id() { return category_id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
}


