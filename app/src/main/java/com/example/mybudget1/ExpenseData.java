package com.example.mybudget1;

public class ExpenseData {
    private String name;      // Название расхода
    private int id;
    private String date;
    private double amount;    // Сумма расхода

    // Конструктор для инициализации данных о расходе
    public ExpenseData(int id ,String name, double amount , String  date) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.amount = amount;
    }

    // Геттер для получения названия расхода
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    // Сеттер для установки названия расхода
    public void setName(String name) {
        this.name = name;
    }

    // Геттер для получения суммы расхода
    public double getAmount() {
        return amount;
    }

    // Сеттер для установки суммы расхода
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setdate(String date){
        this.date = date;
    }

    @Override
    public String toString() {
        return  "name: " + name  +
                "\namount: " + amount +
                "\ndate: " + date;
    }
}

