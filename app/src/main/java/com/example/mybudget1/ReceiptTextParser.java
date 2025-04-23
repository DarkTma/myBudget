package com.example.mybudget1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReceiptTextParser {

    public static String parseToJSON(String rawText) {
        String[] lines = rawText.split("\n");
        JSONArray items = new JSONArray();

        for (String line : lines) {
            String[] parts = line.split(" ");
            if (parts.length >= 2) {
                String name = parts[0];
                double price = parseDoubleSafe(parts[1]);
                int count = 1;
                if (parts.length >= 3 && parts[2].toLowerCase().startsWith("x")) {
                    count = parseIntSafe(parts[2].substring(1));
                }

                JSONObject item = new JSONObject();
                try {
                    item.put("name", name);
                    item.put("price", price);
                    item.put("count", count);
                    items.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return items.toString();
    }

    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

