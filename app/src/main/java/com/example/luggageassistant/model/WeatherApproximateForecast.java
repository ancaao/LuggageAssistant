package com.example.luggageassistant.model;

public class WeatherApproximateForecast {
    public String date; // în format yyyy-MM-dd
    public float minTemp;
    public float maxTemp;
    public String city;

    public WeatherApproximateForecast(String date, float minTemp, float maxTemp, String city) {
        this.date = date;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.city = city;
    }
}
