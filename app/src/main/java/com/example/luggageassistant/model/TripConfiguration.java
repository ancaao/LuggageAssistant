package com.example.luggageassistant.model;

import java.util.Date;
import java.util.List;

public class TripConfiguration {
    // step 1: personal information
    private int age;
    private String gender;
    private List<String> specialPreferences;
    private List<TravelPartner> partner;

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

    public static class Luggage {
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

    public static class TravelPartner {
        private int age;
        private String gender;
        private List<String> specialPreferences;

        public TravelPartner() {
        }

        public TravelPartner(int age, String gender, List<String> specialPreferences) {
            this.age = age;
            this.gender = gender;
            this.specialPreferences = specialPreferences;
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
        return partner;
    }

    public void setPartner(List<TravelPartner> partner) {
        this.partner = partner;
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
