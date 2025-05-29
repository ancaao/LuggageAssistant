package com.example.luggageassistant.utils;

import android.net.ParseException;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import com.example.luggageassistant.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InputValidator implements TextWatcher {
    private final TextInputLayout layout;
    private final TextInputEditText editText;
    private final Context context;

    public InputValidator(Context context, TextInputLayout layout, TextInputEditText editText) {
        this.context = context;
        this.layout = layout;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        validateField();
    }

    @Override
    public void afterTextChanged(Editable s) {}

    public boolean validateField() {
        String input = editText.getText().toString().trim();
        boolean isValid = false;

        if (editText.getId() == R.id.email) {
            isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches();
            layout.setError(isValid ? null : "Invalid email address!");
        }
        else if (editText.getId() == R.id.password) {
            String passwordPattern = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}";
            isValid = input.matches(passwordPattern);
            layout.setError(isValid ? null : "Password must contain: 8 characters, uppercase letter, lowercase letter, number, special character.");
        }
        else if (editText.getId() == R.id.phoneNo) {
            String cleanedPhoneNumber = input.replaceAll("[^0-9]", "");

            if (!input.matches("[0-9\\+\\- ]*")) {
                layout.setError("Phone number should contain only digits, '+', '-', or spaces!");
            } else if (cleanedPhoneNumber.length() < 9 || cleanedPhoneNumber.length() > 16) {
                layout.setError("Phone number must contain between 9 and 16 digits!");
            } else {
                layout.setError(null);
                isValid = true;
            }
        }

        int color = isValid ? ContextCompat.getColor(context, R.color.success)
                : ContextCompat.getColor(context, R.color.error);
        layout.setBoxStrokeColor(color);

        return isValid;
    }

    public static boolean isNameValid(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isAgeValid(String ageText) {
        try {
            int age = Integer.parseInt(ageText.trim());
            return age >= 0 && age <= 120;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isGenderValid(int selectedGenderId) {
        return selectedGenderId != -1;
    }

    public static boolean isFieldNotEmpty(TextInputLayout layout) {
         EditText editText = (EditText) layout.getEditText();
        if (editText == null) return false;

        String input = editText.getText() != null ? editText.getText().toString().trim() : "";
        if (input.isEmpty()) {
            layout.setError("This field is required");
//            layout.setBoxStrokeColor(ContextCompat.getColor(layout.getContext(), R.color.error));
            return false;
        } else {
            layout.setError(null);
//            layout.setBoxStrokeColor(ContextCompat.getColor(layout.getContext(), R.color.success));
            return true;
        }
    }
    public static boolean isCountrySelected(Button countryButton, TextView errorTextView) {
        String country = countryButton.getText().toString().trim();
        if (country.equals("Select Country")) {
            errorTextView.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorTextView.setVisibility(View.GONE);
            return true;
        }
    }

    public static boolean isButtonSelectionValid(Button button, TextView errorTextView, String defaultText) {
        String selectedText = button.getText().toString().trim();
        if (selectedText.equals(defaultText)) {
            errorTextView.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorTextView.setVisibility(View.GONE);
            return true;
        }
    }
    public static boolean isEndDateAfterOrEqual(String startDateStr, String endDateStr, TextInputEditText endDateField, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDateStr);
            Date end = sdf.parse(endDateStr);
            if (start != null && end != null && end.before(start)) {
                endDateField.setError("End date must be after or equal to start date");
//                endDateField.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.error));
                return false;
            } else {
                endDateField.setError(null);
//                endDateField.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.success));
                return true;
            }
        } catch (java.text.ParseException e) {
            endDateField.setError("Invalid date format");
            return false;
        }
    }
//    public static boolean isStartDateAfterPreviousEnd(String currentStartDateStr, String previousEndDateStr, TextInputEditText startDateField, Context context) {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        try {
//            Date prevEnd = sdf.parse(previousEndDateStr);
//            Date currStart = sdf.parse(currentStartDateStr);
//            if (currStart != null && prevEnd != null && !currStart.after(prevEnd)) {
//                startDateField.setError("Start date must be after previous destination's end date");
////                startDateField.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.error));
//                return false;
//            } else {
//                startDateField.setError(null);
////                startDateField.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.success));
//                return true;
//            }
//        } catch (java.text.ParseException e) {
//            startDateField.setError("Invalid date format");
//            return false;
//        }
//    }
}
