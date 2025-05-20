package com.example.mybudget1;

public class CardItem {
    int iconResId;
    String text;

    public CardItem(int iconResId, String text) {
        this.iconResId = iconResId;
        this.text = text;
    }

    public int getIconResId() { return iconResId; }
    public String getText() { return text; }
}
