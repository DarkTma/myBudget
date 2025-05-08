package com.example.mybudget1;

public class DayItem {
    private int id;
    private String name;
    private double spent;
    private boolean isDone;

    public DayItem(int id, String name, double spent, boolean isDone) {
        this.id = id;
        this.name = name;
        this.spent = spent;
        this.isDone = isDone;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getSpent() {
        return spent;
    }

    public boolean isDone() {
        return isDone;
    }

    // Сеттеры (если нужно редактировать)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return name + " - " + spent + " - " + isDone;
    }
}

