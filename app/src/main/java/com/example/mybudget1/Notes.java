package com.example.mybudget1;

public class Notes {
    private int id; // ID из базы данных
    private String triggerAtMillis;
    private String name;
    private String type;
    private String action;

    public Notes(int id, String triggerAtMillis, String name, String type, String action) {
        this.id = id;
        this.triggerAtMillis = triggerAtMillis;
        this.name = name;
        this.type = type;
        this.action = action;
    }

    public int getId() {
        return id;
    }

    public String getTriggerAtMillis() {
        return triggerAtMillis;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTriggerAtMillis(String triggerAtMillis) {
        this.triggerAtMillis = triggerAtMillis;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {return action;}
}


