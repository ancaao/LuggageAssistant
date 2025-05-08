package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PackingListEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PackingListParser {

    public static List<PackingListEntry> parseGrouped(String rawText) throws Exception {
        int jsonStart = rawText.indexOf("{");
        if (jsonStart == -1) throw new Exception("No JSON object found.");

        String jsonOnly = rawText.substring(jsonStart);
        JSONObject root = new JSONObject(jsonOnly);

        List<PackingListEntry> entries = new ArrayList<>();

        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String category = keys.next();
            entries.add(new PackingListEntry(PackingListEntry.TYPE_CATEGORY, category, null));

            JSONArray itemsArray = root.getJSONArray(category);
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject obj = itemsArray.getJSONObject(i);
                String item = obj.getString("item");
                int quantity = obj.getInt("quantity");
                PackingItem packingItem = new PackingItem(category, item, quantity);
                entries.add(new PackingListEntry(PackingListEntry.TYPE_ITEM, null, packingItem));
            }
        }
        return entries;
    }
}