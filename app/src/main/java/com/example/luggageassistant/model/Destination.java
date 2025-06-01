package com.example.luggageassistant.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Destination implements Serializable {
    private String country;
    private String city;
    private String tripStartDate;
    private String tripEndDate;

    public Destination() {}

    public Destination(String country, String city, String tripStartDate, String tripEndDate) {
        this.country = country;
        this.city = city;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("country", country);
        result.put("city", city);
        result.put("tripStartDate", tripStartDate);
        result.put("tripEndDate", tripEndDate);
        return result;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(String tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public String getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(String tripEndDate) {
        this.tripEndDate = tripEndDate;
    }
}