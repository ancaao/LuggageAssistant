package com.example.luggageassistant.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Luggage {
    private String luggageType;
    private String dimensionLimit;
    private String weightLimit;

    public Luggage() {}

    public Luggage(String bagType, String luggageCategory, String dimensionLimit,
                   String weightLimit, List<String> specialAccessories) {
        this.luggageType = bagType;
        this.dimensionLimit = dimensionLimit;
        this.weightLimit = weightLimit;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("luggageType", luggageType);
        result.put("dimensionLimit", dimensionLimit);
        result.put("weightLimit", weightLimit);
        return result;
    }
    public String getDimensionLimit() {
        return dimensionLimit;
    }

    public void setDimensionLimit(String dimensionLimit) {
        this.dimensionLimit = dimensionLimit;
    }

    public String getLuggageType() {
        return luggageType;
    }

    public void setLuggageType(String luggageType) {
        this.luggageType = luggageType;
    }

    public String getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(String weightLimit) {
        this.weightLimit = weightLimit;
    }
}
