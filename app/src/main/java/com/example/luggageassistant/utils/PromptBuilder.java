package com.example.luggageassistant.utils;

import com.example.luggageassistant.model.TripConfiguration;

import org.json.JSONObject;

public class PromptBuilder {
    public static String buildPromptFromTrip(TripConfiguration trip) {
        try {
            JSONObject jsonObject = new JSONObject(trip.toMap());

            return "Return only a raw JSON object (not a string) with this exact structure:\n\n" +
                    "{\n" +
                    "  \"PersonName\": {\n" +
                    "    \"Clothing\": [ { \"item\": \"T-shirt\", \"quantity\": 3 } ],\n" +
                    "    \"Toiletries\": [ { \"item\": \"Toothbrush\", \"quantity\": 1 } ],\n" +
                    "    \"Medications\": [...],\n" +
                    "    \"Documents\": [...],\n" +
                    "    \"Electronics\": [...],\n" +
                    "    \"Special accessories\": [...],\n" +
                    "    \"Special preferences\": [...],\n" +
                    "    \"Other\": [...]\n" +
                    "  }\n" +
                    "}\n\n" +
                    "Important:\n" +
                    "- Respond with a JSON object only, NOT inside a string.\n" +
                    "- Do not include any explanation or extra text.\n" +
                    "- Do not assume shared items between people.\n" +
                    "- If special accessories or preferences has no items (is null), don't include them in the generated list json object.\n\n" +
                    "- Consider all the things each person could need and what a person with the same details usually packs." +
                    "- Include at least one item in each category from above (except Special accessories and Special preferences. Include those only if they are not null in the trip configuration)." +
                    "- The list should be as detailed as possible, covering the needs of each person considering all the information in the trip configuration below." +
                    "Trip configuration:\n" + jsonObject.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error building the prompt.";
        }
    }
}
