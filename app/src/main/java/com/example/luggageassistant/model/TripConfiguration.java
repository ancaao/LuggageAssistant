package com.example.luggageassistant.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripConfiguration {
    // step 1: personal information
    private int age;
    private String gender;
    private List<String> specialPreferences;
    private List<TravelPartner> partners;

    // step 2: luggage information
    private int numberOfLuggages;
    private List<Luggage> luggage;
    private List<String> specialAccessories;

    // step 3: trip details
    private String country;
    private String city;
    private Date tripStartDate;
    private Date tripEndDate;
    private String transportType;

    // step 4: trip purpose
    private String travelPurpose;
    private List<String> plannedActivities;
    private List<String> specialEvents;

    public TripConfiguration() {
    }
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("age", age);
        result.put("gender", gender);
        result.put("specialPreferences", specialPreferences);
        result.put("numberOfLuggages", numberOfLuggages);
        result.put("luggage", luggage != null ? luggage.stream().map(Luggage::toMap).collect(Collectors.toList()) : null);
        result.put("specialAccessories", specialAccessories);
        result.put("country", country);
        result.put("city", city);
        result.put("tripStartDate", tripStartDate);
        result.put("tripEndDate", tripEndDate);
        result.put("transportType", transportType);
        result.put("travelPurpose", travelPurpose);
        result.put("plannedActivities", plannedActivities);
        result.put("specialEvents", specialEvents);
        result.put("partners", partners != null ? partners.stream().map(TravelPartner::toMap).collect(Collectors.toList()) : null);
        return result;
    }

    public void updateFromStepOne(int age, String gender, List<String> preferences, List<TravelPartner> partners) {
        this.age = age;
        this.gender = gender;
        this.specialPreferences = preferences;
        this.partners = partners;

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

    public int getNumberOfLuggages() {
        return numberOfLuggages;
    }

    public void setNumberOfLuggages(int numberOfLuggages) {
        this.numberOfLuggages = numberOfLuggages;
    }

    public List<Luggage> getLuggage() {
        return luggage;
    }

    public void setLuggage(List<Luggage> luggage) {
        this.luggage = luggage;
    }

    public List<String> getSpecialAccessories() {
        return specialAccessories;
    }

    public void setSpecialAccessories(List<String> specialAccessories) {
        this.specialAccessories = specialAccessories;
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

    public Date getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(Date tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public Date getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(Date tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
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
