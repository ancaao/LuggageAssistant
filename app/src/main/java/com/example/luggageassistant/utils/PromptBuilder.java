package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.TripConfiguration;

import org.json.JSONObject;

public class PromptBuilder {
    public static String buildPromptFromTrip(TripConfiguration trip) {
        try {
            // Transforma Map-ul într-un JSONObject și apoi într-un string JSON
            JSONObject jsonObject = new JSONObject(trip.toMap());

            // Construcția promptului complet pentru GPT
            return "Generate a very detailed JSON list of everything a luggage could contain for a person or more " +
                    "traveling according to the trip configuration below." +
                    "Don't assume they share their personal items." +
                    "Include categories toiletries, clothing, medication, documents, electronics, " +
                    "special accessories (only if not null), special preferences (only if not null) and other." +
                    "The format or your response should look like" +
                    "{\n" +
                    "  \"Name\": {\n" +
                    "    \"Clothing\": [ { \"item\": \"t-shit\", \"quantity\": 3 } ],\n" +
                    "    \"Toiletries\": [ {...} ]\n" +
                    "  },\n" +
                    "  \"Name\": {\n" +
                    "    \"Clothing\": [ {...} ]\n" +
                    "  }\n" +
                    "}\n" +
                    jsonObject.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error building the prompt.";
        }
    }
}
