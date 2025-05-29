package com.example.luggageassistant.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

public class DateValidatorInclusive implements CalendarConstraints.DateValidator {

    private final long minDate;

    public DateValidatorInclusive(long minDate) {
        this.minDate = minDate;
    }

    @Override
    public boolean isValid(long date) {
        return date >= minDate; // Acceptă date egale sau după
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(minDate);
    }

    public static final Parcelable.Creator<DateValidatorInclusive> CREATOR = new Parcelable.Creator<DateValidatorInclusive>() {
        @Override
        public DateValidatorInclusive createFromParcel(Parcel in) {
            long minDate = in.readLong();
            return new DateValidatorInclusive(minDate);
        }

        @Override
        public DateValidatorInclusive[] newArray(int size) {
            return new DateValidatorInclusive[size];
        }
    };
}
