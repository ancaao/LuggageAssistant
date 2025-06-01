package com.example.luggageassistant.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripConfiguration implements Serializable {
    private String tripId;
    private boolean pinned = false;
    // step 1: personal information
    private String name;
    private int age;
    private String gender;
    private List<String> specialPreferences;
    private List<TravelPartner> partners;

    // step 2: luggage information
    private List<Luggage> luggages;

    // step 3: trip details
    private List<Destination> destinations;

    // step 4: trip purpose
    private List<String> travelPurpose;
    private List<String> plannedActivities;
    private List<String> specialEvents;

    public TripConfiguration() {
    }
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("pinned", pinned);
        result.put("name", name);
        result.put("age", age);
        result.put("gender", gender);
        result.put("specialPreferences", specialPreferences);
        result.put("luggages", luggages != null ? luggages.stream().map(Luggage::toMap).collect(Collectors.toList()) : null);
        result.put("destinations", destinations != null ? destinations.stream().map(Destination::toMap).collect(Collectors.toList()) : null);
        result.put("travelPurpose", travelPurpose);
        result.put("plannedActivities", plannedActivities);
        result.put("specialEvents", specialEvents);
        result.put("partners", partners != null ? partners.stream().map(TravelPartner::toMap).collect(Collectors.toList()) : null);
        return result;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
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

    public List<Destination> getDestinations() {
        return destinations;
    }
    public void setDestinations(List<Destination> destination) {
        this.destinations = destination;
    }
    public Destination getFirstDestination() {
        return (destinations != null && !destinations.isEmpty()) ? destinations.get(0) : null;
    }

    public List<String> getTravelPurpose() {
        return travelPurpose;
    }

    public void setTravelPurpose(List<String> travelPurpose) {
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
