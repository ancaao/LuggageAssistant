package com.example.luggageassistant.model;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelPartner {
    private String name;
    private int age;
    private String gender;
    private List<String> specialPreferences;

    public TravelPartner() {
    }

    public TravelPartner(String name, int age, String gender, List<String> specialPreferences) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.specialPreferences = specialPreferences;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("gender", gender);
        result.put("specialPreferences", specialPreferences);
        return result;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getSpecialPreferences() {
        return specialPreferences;
    }

    public void setSpecialPreferences(List<String> specialPreferences) {
        this.specialPreferences = specialPreferences;
    }
}