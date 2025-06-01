package com.example.luggageassistant.utils;

import android.annotation.SuppressLint;

import com.example.luggageassistant.model.Destination;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GetAllTripData {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String getAllCities(List<Destination> destinations) {
        if (destinations == null) return "-";
        Set<String> cities = new LinkedHashSet<>();
        for (Destination d : destinations) {
            if (d.getCity() != null) cities.add(d.getCity());
        }

        return buildLimitedString(cities, 21);
    }

    public static String getAllCountries(List<Destination> destinations) {
        if (destinations == null) return "-";
        Set<String> countries = new LinkedHashSet<>();
        for (Destination d : destinations) {
            if (d.getCountry() != null) countries.add(d.getCountry());
        }

        return buildLimitedString(countries, 21);
    }

    public static String buildLimitedString(Set<String> items, int maxLength) {
        StringBuilder result = new StringBuilder();
        int countIncluded = 0;
        int totalLength = 0;

        List<String> itemList = new ArrayList<>(items);
        for (String item : itemList) {
            int lengthWithComma = item.length() + (countIncluded > 0 ? 2 : 0); // ", " separator if not first

            if (totalLength + lengthWithComma > maxLength) break;

            if (countIncluded > 0) result.append(", ");
            result.append(item);
            totalLength += lengthWithComma;
            countIncluded++;
        }

        int remaining = items.size() - countIncluded;
        if (remaining > 0) {
            result.append(" +").append(remaining);
        }

        return result.length() > 0 ? result.toString() : "-";
    }

    @SuppressLint("NewApi")
    public static String getEarliestStartDate(List<Destination> destinations) {
        if (destinations == null) return "-";
        LocalDate minDate = null;
        for (Destination d : destinations) {
            try {
                LocalDate date = LocalDate.parse(d.getTripStartDate(), formatter);
                if (minDate == null || date.isBefore(minDate)) minDate = date;
            } catch (Exception ignored) {}
        }
        return minDate != null ? minDate.format(formatter) : "-";
    }

    @SuppressLint("NewApi")
    public static String getLatestEndDate(List<Destination> destinations) {
        if (destinations == null) return "-";
        LocalDate maxDate = null;
        for (Destination d : destinations) {
            try {
                LocalDate date = LocalDate.parse(d.getTripEndDate(), formatter);
                if (maxDate == null || date.isAfter(maxDate)) maxDate = date;
            } catch (Exception ignored) {}
        }
        return maxDate != null ? maxDate.format(formatter) : "-";
    }
}