package com.example.mybudget1;

public class ExpenseData {
    private String name;      // Название расхода

    private String date;
    private double amount;    // Сумма расхода

    // Конструктор для инициализации данных о расходе
    public ExpenseData(String name, double amount , String  date) {
        this.name = name;
        this.date = date;
        this.amount = amount;
    }

    // Геттер для получения названия расхода
    public String getName() {
        return name;
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

    public String getdate() {
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

