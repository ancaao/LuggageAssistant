package com.example.luggageassistant.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CountryCode {

    private static final Map<String, String> countryToIsoMap = new HashMap<>();
    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) return;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("countries.json"); // numele fișierului tău

            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String json = scanner.hasNext() ? scanner.next() : "";

            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("name");
                String code = obj.getString("code");
                countryToIsoMap.put(name, code.toLowerCase()); // ex: "France" -> "fr"
            }

            initialized = true;

        } catch (Exception e) {
            Log.e("CountryCodeUtils", "Failed to load country codes", e);
        }
    }

    public static String getIso2Code(String countryName) {
        return countryToIsoMap.getOrDefault(countryName, null);
    }
}