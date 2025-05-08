package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.TripConfiguration;

import org.json.JSONObject;

public class PromptBuilder {
    public static String buildPromptFromTrip(TripConfiguration trip) {
        try {
            // Transforma Map-ul într-un JSONObject și apoi într-un string JSON
            JSONObject jsonObject = new JSONObject(trip.toMap());

            // Construcția promptului complet pentru GPT
            return "Generate a detailed JSON list of what a luggage should contain for a person or more traveling according to the trip configuration below." +
                    "The format or your response should look like" +
                    "{\n" +
                    "  \"Clothing\": [ { \"item\": \"pantaloni\", \"quantity\": 2 }, ... ],\n" +
                    "  \"Toiletries\": [ ... ]\n" +
                    "}\n\n" +
                    jsonObject.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error building the prompt.";
        }
    }
}
