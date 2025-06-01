package com.example.luggageassistant.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Luggage implements Serializable {
    private List<String> owners;
    private String luggageType;
    private int length;
    private int width;
    private int height;
    private int weight;
    private List<String> specialAccessories;

    public Luggage() {}

    public Luggage(List <String> owners, String luggageType, int length,
                   int width, int height, int weight, List<String> specialAccessories) {
        this.owners = owners;
        this.luggageType = luggageType;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.specialAccessories = specialAccessories;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("owners", owners);
        result.put("luggageType", luggageType);
        result.put("length", length);
        result.put("width", width);
        result.put("height", height);
        result.put("weight", weight);
        result.put("specialAccessories", specialAccessories);
        return result;
    }

    public String getLuggageType() {
        return luggageType;
    }

    public void setLuggageType(String luggageType) {
        this.luggageType = luggageType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<String> getSpecialAccessories() {
        return specialAccessories;
    }

    public void setSpecialAccessories(List<String> specialAccessories) {
        this.specialAccessories = specialAccessories;
    }
    public List<String> getOwners() {
        return owners;
    }
    public void setOwners(List<String> owners) {
        this.owners = owners;
    }
}
