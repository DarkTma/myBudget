package com.example.mybudget1;

public class Goal {
    private int id;
    private String name;
    private double amount;
    public double currentAmount;
    private String imagePath;

    public Goal(int id, String name, double amount, double currentAmount, String imagePath) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currentAmount = currentAmount;
        this.imagePath = imagePath;
    }

    public Goal(String name, double amount, double currentAmount, String imagePath) {
        this.name = name;
        this.amount = amount;
        this.currentAmount = currentAmount;
        this.imagePath = imagePath;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getImagePath() { return imagePath; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}


