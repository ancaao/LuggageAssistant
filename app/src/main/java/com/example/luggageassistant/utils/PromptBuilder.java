package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.TripConfiguration;

import org.json.JSONObject;

public class PromptBuilder {
    public static String buildPromptFromTrip(TripConfiguration trip) {
        try {
            // Transforma Map-ul într-un JSONObject și apoi într-un string JSON
            JSONObject jsonObject = new JSONObject(trip.toMap());

            // Construcția promptului complet pentru GPT
            return "Generate a detailed list of what a luggage should contain for a person or more traveling according to this trip configuration:" + jsonObject.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            return "Eroare la construirea promptului";
        }
    }
}
