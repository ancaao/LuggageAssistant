package com.example.luggageassistant.model;

import java.time.LocalDate;

public class CityDateRange {
    public String city;
    public String country;
    public LocalDate startDate;
    public LocalDate endDate;

    public CityDateRange(String city, String country, LocalDate startDate, LocalDate endDate) {
        this.city = city;
        this.country = country;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
