package com.example.luggageassistant.utils;

import android.annotation.SuppressLint;
import android.net.ParseException;

import com.example.luggageassistant.model.CityDateRange;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;

import java.text.SimpleDateFormat;
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
            int lengthWithComma = item.length() + (countIncluded > 0 ? 2 : 0);

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

    public static Date getFirstTripStartDate(TripConfiguration trip) {
        List<Destination> destinations = trip.getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            String dateStr = destinations.get(0).getTripStartDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    public static List<CityDateRange> extractCityDateRanges(List<Destination> destinations) {
        List<CityDateRange> result = new ArrayList<>();
        if (destinations == null) return result;

        for (Destination d : destinations) {
            try {
                LocalDate start = LocalDate.parse(d.getTripStartDate(), formatter);
                LocalDate end = LocalDate.parse(d.getTripEndDate(), formatter);
                result.add(new CityDateRange(d.getCity(), d.getCountry(), start, end));
            } catch (Exception ignored) {}
        }

        return result;
    }

    public static class CategorizedTrips {
        public List<TripConfiguration> pinned;
        public List<TripConfiguration> upcoming;
        public List<TripConfiguration> past;

        public CategorizedTrips(List<TripConfiguration> pinned, List<TripConfiguration> upcoming, List<TripConfiguration> past) {
            this.pinned = pinned;
            this.upcoming = upcoming;
            this.past = past;
        }
    }

    public static CategorizedTrips categorizeTrips(List<TripConfiguration> trips) {
        List<TripConfiguration> pinned = new ArrayList<>();
        List<TripConfiguration> upcoming = new ArrayList<>();
        List<TripConfiguration> past = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (TripConfiguration trip : trips) {
            Date startDate = getFirstTripStartDate(trip);
            if (startDate == null) continue;

            if (trip.isPinned()) {
                pinned.add(trip);
            } else if (!startDate.before(today)) {
                upcoming.add(trip);
            } else {
                past.add(trip);
            }
        }

        Comparator<TripConfiguration> dateComparator = Comparator.comparing(
                trip -> {
                    List<Destination> destinations = trip.getDestinations();
                    if (destinations != null && !destinations.isEmpty()) {
                        String dateStr = destinations.get(0).getTripStartDate();
                        if (dateStr != null && !dateStr.isEmpty()) {
                            try {
                                return sdf.parse(dateStr);
                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return null;
                },
                Comparator.nullsLast(Date::compareTo)
        );

        pinned.sort(dateComparator);
        upcoming.sort(dateComparator);
        past.sort(dateComparator.reversed());

        return new CategorizedTrips(pinned, upcoming, past);
    }

}