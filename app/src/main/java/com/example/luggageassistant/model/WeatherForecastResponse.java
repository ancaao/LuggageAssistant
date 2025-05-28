package com.example.luggageassistant.model;

import java.util.List;

public class WeatherForecastResponse {
    public List<ForecastDay> list;

    public static class ForecastDay {
        public Temp temp;
        public List<Weather> weather;
    }

    public static class Temp {
        public float min;
        public float max;
    }

    public static class Weather {
        public String main;
        public String description;
        public String icon;
    }
}
