package com.example.luggageassistant.utils;

import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import com.example.luggageassistant.R;

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
        TextInputEditText editText = (TextInputEditText) layout.getEditText();
        if (editText == null) return false;

        String input = editText.getText() != null ? editText.getText().toString().trim() : "";
        if (input.isEmpty()) {
            layout.setError("This field is required");
            layout.setBoxStrokeColor(ContextCompat.getColor(layout.getContext(), R.color.error));
            return false;
        } else {
            layout.setError(null);
            layout.setBoxStrokeColor(ContextCompat.getColor(layout.getContext(), R.color.success));
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


}
