package com.example.luggageassistant.model;

import java.util.List;

public class OneCallResponse {
    public List<DailyForecast> daily;

    public static class DailyForecast {
        public long dt;
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
