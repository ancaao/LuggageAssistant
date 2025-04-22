package com.example.luggageassistant.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripConfiguration {
    // step 1: personal information
    private String name;
    private int age;
    private String gender;
    private List<String> specialPreferences;
    private List<TravelPartner> partners;

    // step 2: luggage information
    private List<Luggage> luggages;

    // step 3: trip details
    private String country;
    private String city;
    private String tripStartDate;
    private String tripEndDate;

    // step 4: trip purpose
    private String travelPurpose;
    private List<String> plannedActivities;
    private List<String> specialEvents;

    public TripConfiguration() {
    }
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("age", age);
        result.put("gender", gender);
        result.put("specialPreferences", specialPreferences);
        result.put("luggages", luggages != null ? luggages.stream().map(Luggage::toMap).collect(Collectors.toList()) : null);
        result.put("country", country);
        result.put("city", city);
        result.put("tripStartDate", tripStartDate);
        result.put("tripEndDate", tripEndDate);
        result.put("travelPurpose", travelPurpose);
        result.put("plannedActivities", plannedActivities);
        result.put("specialEvents", specialEvents);
        result.put("partners", partners != null ? partners.stream().map(TravelPartner::toMap).collect(Collectors.toList()) : null);
        return result;
    }

    public void updateFromStepOne(String name, int age, String gender, List<String> preferences, List<TravelPartner> partners) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.specialPreferences = preferences;
        this.partners = partners;

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

    public List<TravelPartner> getPartner() {
        return partners;
    }

    public void setPartner(List<TravelPartner> partners) {
        this.partners = partners;
    }

    public List<Luggage> getLuggage() {
        return luggages;
    }

    public void setLuggage(List<Luggage> luggage) {
        this.luggages = luggage;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getTravelPurpose() {
        return travelPurpose;
    }

    public void setTravelPurpose(String travelPurpose) {
        this.travelPurpose = travelPurpose;
    }

    public List<String> getPlannedActivities() {
        return plannedActivities;
    }

    public void setPlannedActivities(List<String> plannedActivities) {
        this.plannedActivities = plannedActivities;
    }

    public List<String> getSpecialEvents() {
        return specialEvents;
    }

    public void setSpecialEvents(List<String> specialEvents) {
        this.specialEvents = specialEvents;
    }
}
