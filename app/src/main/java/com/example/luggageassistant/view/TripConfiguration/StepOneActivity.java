package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.utils.InputValidator;
import com.example.luggageassistant.utils.StepperUtils;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepOneActivity extends AppCompatActivity {
    private LinearLayout partnerContainer;
    private Button addPartnerButton, selectSpecialPreferencesButton, nextButton, cancelButton;
    private List<String> userSelectedItems = new ArrayList<>();
    private TripConfigurationViewModel tripConfigurationViewModel;
    private Map<View, List<String>> partnerPreferencesMap = new HashMap<>();
    private TextView specialPreferencesSummary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_one);

        StepperUtils.configureStep(this, 1);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);
        tripConfigurationViewModel.messageLiveData.observe(this, this::displayMessage);

        partnerContainer = findViewById(R.id.partnerContainer);
        addPartnerButton = findViewById(R.id.addPartnerButton);
        selectSpecialPreferencesButton = findViewById(R.id.selectSpecialPreferencesButton);
        nextButton = findViewById(R.id.stepOneNextButton);
        cancelButton = findViewById(R.id.stepOneCancelButton);
        specialPreferencesSummary = findViewById(R.id.specialPreferencesSummary);

        selectSpecialPreferencesButton.setOnClickListener(view -> showSpecialPreferencesDialog(null));
        addPartnerButton.setOnClickListener(view -> addPartnerFields());

        nextButton.setOnClickListener(view -> {
            TextInputLayout nameLayout = findViewById(R.id.nameInputLayout);
            TextInputEditText nameInput = findViewById(R.id.nameInput);
            TextInputLayout ageLayout = findViewById(R.id.ageInputLayout);
            TextInputEditText ageInput = findViewById(R.id.ageInput);
            RadioGroup genderGroup = findViewById(R.id.genderGroup);
            TextView genderErrorText = findViewById(R.id.genderErrorText);

            String name = nameInput.getText().toString().trim();
            String ageText = ageInput.getText().toString().trim();
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            RadioButton genderButton = findViewById(selectedGenderId);
            String gender = (genderButton != null) ? genderButton.getText().toString() : "";

            boolean validUser = true;

            if (!InputValidator.isNameValid(name)) {
                nameLayout.setError("Name cannot be empty.");
                validUser = false;
            } else {
                nameLayout.setError(null);
            }

            if (!InputValidator.isAgeValid(ageText)) {
                ageLayout.setError("Enter a valid age.");
                validUser = false;
            } else {
                ageLayout.setError(null);
            }

            if (!InputValidator.isGenderValid(selectedGenderId)) {
                genderErrorText.setText("Please select a gender.");
                genderErrorText.setVisibility(View.VISIBLE);
                validUser = false;
            } else {
                genderErrorText.setText("");
                genderErrorText.setVisibility(View.GONE);
            }

            // Validează și partenerii — chiar dacă userul e invalid
            List<TravelPartner> partners = collectPartnersData();
            boolean validPartners = partners != null;

            if (!validUser || !validPartners) {
                return; // ieșim doar după ce am verificat tot
            }

            int age = Integer.parseInt(ageText);
            List<String> preferences = new ArrayList<>(userSelectedItems);

            tripConfigurationViewModel.updateFormStepOne(name, age, gender, preferences, partners);

            Intent intent = new Intent(this, StepTwoActivity.class);
            startActivity(intent);
        });


        cancelButton.setOnClickListener(view -> {
            tripConfigurationViewModel.resetTripConfiguration();
            finish();
        });
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void addPartnerFields() {
        View partnerView = LayoutInflater.from(this).inflate(R.layout.activity_form_partner_fields, partnerContainer, false);

        Button removePartnerButton = partnerView.findViewById(R.id.removePartnerButton);
        Button selectPreferencesButton = partnerView.findViewById(R.id.partnerSelectSpecialPreferencesButton);
        TextView partnerPreferencesSummary = partnerView.findViewById(R.id.partnerPreferencesSummary);

        // Dialogul pentru preferințele partenerului
        selectPreferencesButton.setOnClickListener(v -> showSpecialPreferencesDialog(partnerView));

        // Actualizare și afișare preferințe selectate
        partnerPreferencesSummary.setOnClickListener(v -> showSpecialPreferencesDialog(partnerView));

        removePartnerButton.setOnClickListener(view -> partnerContainer.removeView(partnerView));

        partnerContainer.addView(partnerView);
    }

    private void showSpecialPreferencesDialog(View sourceView) {
        String[] items = getResources().getStringArray(R.array.special_preferences);
        boolean[] checkedItems = new boolean[items.length];
        List<String> selectedPreferences;

        if (sourceView == null) {
            selectedPreferences = new ArrayList<>(userSelectedItems);
        } else {
            selectedPreferences = partnerPreferencesMap.getOrDefault(sourceView, new ArrayList<>());
        }

        for (int i = 0; i < items.length; i++) {
            checkedItems[i] = selectedPreferences.contains(items[i]);
        }

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Add new preference");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Preferences");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);

        builder.setView(layout);
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedPreferences.contains(items[which])) {
                    selectedPreferences.add(items[which]);
                }
            } else {
                selectedPreferences.remove(items[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newItem = input.getText().toString().trim();
            if (!newItem.isEmpty()) {
                selectedPreferences.add(newItem);
            }

            if (sourceView == null) {
                userSelectedItems.clear();
                userSelectedItems.addAll(selectedPreferences);

                if (specialPreferencesSummary != null) {
                    String summary = selectedPreferences.isEmpty() ? ""
                            : "Selected: " + String.join(", ", selectedPreferences);
                    specialPreferencesSummary.setText(summary);
                }
            } else {
                partnerPreferencesMap.put(sourceView, new ArrayList<>(selectedPreferences));

                TextView partnerPreferencesSummary = sourceView.findViewById(R.id.partnerPreferencesSummary);
                if (partnerPreferencesSummary != null) {
                    String summary = selectedPreferences.isEmpty()
                            ? "No preferences selected"
                            : "Selected: " + String.join(", ", selectedPreferences);
                    partnerPreferencesSummary.setText(summary);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private List<String> getSpecialPreferencesForPartner(View partnerView) {
        List<String> preferences = partnerPreferencesMap.get(partnerView);
        if (preferences == null) return new ArrayList<>();
        return preferences;
    }

    private List<TravelPartner> collectPartnersData() {
        List<TravelPartner> partners = new ArrayList<>();
        boolean allValid = true;

        for (int i = 0; i < partnerContainer.getChildCount(); i++) {
            View partnerView = partnerContainer.getChildAt(i);

            TextInputLayout nameLayout = partnerView.findViewById(R.id.partnerNameInputLayout);
            TextInputEditText nameInput = partnerView.findViewById(R.id.partnerNameInput);
            TextInputLayout ageLayout = partnerView.findViewById(R.id.partnerAgeInputLayout);
            TextInputEditText ageInput = partnerView.findViewById(R.id.partnerAgeInput);
            RadioGroup genderGroup = partnerView.findViewById(R.id.partnerGenderGroup);
            TextView genderErrorText = partnerView.findViewById(R.id.partnerGenderErrorText);

            String name = nameInput.getText().toString().trim();
            String ageText = ageInput.getText().toString().trim();
            int selectedId = genderGroup.getCheckedRadioButtonId();
            RadioButton genderButton = partnerView.findViewById(selectedId);
            String gender = (genderButton != null) ? genderButton.getText().toString() : "";

            boolean valid = true;

            if (!InputValidator.isNameValid(name)) {
                nameLayout.setError("Name cannot be empty.");
                valid = false;
            } else {
                nameLayout.setError(null);
            }

            if (!InputValidator.isAgeValid(ageText)) {
                ageLayout.setError("Enter a valid age (1–119).");
                valid = false;
            } else {
                ageLayout.setError(null);
            }

            if (!InputValidator.isGenderValid(selectedId)) {
                genderErrorText.setText("Please select a gender.");
                genderErrorText.setVisibility(View.VISIBLE);
                valid = false;
            } else {
                genderErrorText.setText("");
                genderErrorText.setVisibility(View.GONE);
            }

            if (!valid) {
                allValid = false;
                continue;
            }

            int age = Integer.parseInt(ageText);
            List<String> specialPreferences = getSpecialPreferencesForPartner(partnerView);
            TravelPartner partner = new TravelPartner(name, age, gender, specialPreferences);
            partners.add(partner);
        }

        return allValid ? partners : null;
    }
}