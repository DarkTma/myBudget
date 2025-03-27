package com.example.mybudget1;

public class CategoryItem {
    private String name;
    private int price;
    private int id;
    private int procent;

    public CategoryItem(int id, String name, int price , int procent) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.procent = procent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getProcent(){return procent;}

    public void setName(String name) {
        this.name = name;
    }
}
