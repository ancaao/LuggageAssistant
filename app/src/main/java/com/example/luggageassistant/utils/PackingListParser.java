package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PackingListEntry;
import com.example.luggageassistant.model.PersonPackingList;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PackingListParser {

    public static List<PersonPackingList> parsePerPerson(String rawText) throws Exception {
        // Dacă e string serializat, îl deserializăm
        if (rawText.trim().startsWith("\"{")) {
            rawText = new Gson().fromJson(rawText, String.class);
        }

        int jsonStart = rawText.indexOf("{");
        if (jsonStart == -1) throw new Exception("No JSON object found.");

        String jsonOnly = rawText.substring(jsonStart);
        JSONObject root = new JSONObject(jsonOnly);

        List<PersonPackingList> result = new ArrayList<>();

        Iterator<String> people = root.keys();
        while (people.hasNext()) {
            String person = people.next();
            JSONObject personObj = root.getJSONObject(person);
            Map<String, List<PackingItem>> categoryMap = new HashMap<>();

            Iterator<String> categories = personObj.keys();
            while (categories.hasNext()) {
                String category = categories.next();
                JSONArray itemsArray = personObj.getJSONArray(category);
                List<PackingItem> items = new ArrayList<>();

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemObj = itemsArray.getJSONObject(i);
                    String item = itemObj.getString("item");

                    int quantity;
                    try {
                        quantity = itemObj.getInt("quantity");
                    } catch (Exception e) {
                        quantity = 1; // fallback pentru cazuri de genul "As required"
                    }

                    items.add(new PackingItem(category, item, quantity));
                }

                categoryMap.put(category, items);
            }
            result.add(new PersonPackingList(person, categoryMap));
        }
        return result;
    }
}
