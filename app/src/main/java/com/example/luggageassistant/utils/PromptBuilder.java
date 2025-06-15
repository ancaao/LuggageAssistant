package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.TripConfiguration;

import org.json.JSONObject;

public class PromptBuilder {
    public static String buildPromptFromTrip(TripConfiguration trip) {
        try {
            // Transforma Map-ul într-un JSONObject și apoi într-un string JSON
            JSONObject jsonObject = new JSONObject(trip.toMap());

            // Construcția promptului complet pentru GPT
            return "Return only a raw JSON object (not a string) with this exact structure:\n\n" +
                    "{\n" +
                    "  \"PersonName\": {\n" +
                    "    \"Clothing\": [ { \"item\": \"T-shirt\", \"quantity\": 3 } ],\n" +
                    "    \"Toiletries\": [ { \"item\": \"Toothbrush\", \"quantity\": 1 } ],\n" +
                    "    \"Medications\": [...],\n" +
                    "    \"Documents\": [...],\n" +
                    "    \"Electronics\": [...],\n" +
                    "    \"Other\": [...]\n" +
                    "  }\n" +
                    "}\n\n" +
                    "Important:\n" +
                    "- Respond with a JSON object only, NOT inside a string.\n" +
                    "- Do not include any explanation or extra text.\n" +
                    "- Do not assume shared items between people.\n" +
                    "- If special accessories or preferences are null, skip them.\n\n" +
                    "- The list should be as detailed as possible, covering the needs of each person, also considering the luggage dimension." +
                    "Trip configuration:\n" + jsonObject.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error building the prompt.";
        }
    }
}
