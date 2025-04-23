package com.example.mybudget1;

public class MonthData {
    private String monthName;
    private double income;
    private double spent;

    public MonthData(String monthName, double income, double spent) {
        this.monthName = monthName;
        this.income = income;
        this.spent = spent;
    }

    public String getMonthName() {
        return monthName;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public double getSpent() {
        return spent;
    }
}
