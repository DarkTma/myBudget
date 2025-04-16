package com.example.mybudget1;

public class Reminder {
    private int id; // ID из базы данных
    private long triggerAtMillis;
    private String name;
    private int requestCode;

    public Reminder(int id, long triggerAtMillis, String name, int requestCode) {
        this.id = id;
        this.triggerAtMillis = triggerAtMillis;
        this.name = name;
        this.requestCode = requestCode;
    }

    public int getId() {
        return id;
    }

    public long getTriggerAtMillis() {
        return triggerAtMillis;
    }

    public String getName() {
        return name;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTriggerAtMillis(long triggerAtMillis) {
        this.triggerAtMillis = triggerAtMillis;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }
}


