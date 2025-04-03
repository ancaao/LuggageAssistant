package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepOneActivity extends AppCompatActivity {
    private LinearLayout partnerContainer;
    private Button addPartnerButton, selectSpecialPreferencesButton, submitButton;
    private List<String> userSelectedItems = new ArrayList<>();
    private TripConfigurationViewModel tripConfigurationViewModel;

    private Map<View, List<String>> partnerPreferencesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_one);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);
        tripConfigurationViewModel.messageLiveData.observe(this, this::displayMessage);

        partnerContainer = findViewById(R.id.partnerContainer);
        addPartnerButton = findViewById(R.id.addPartnerButton);
        selectSpecialPreferencesButton = findViewById(R.id.selectSpecialPreferencesButton);
        submitButton = findViewById(R.id.submitButton);

        selectSpecialPreferencesButton.setOnClickListener(view -> showSpecialPreferencesDialog(null));
        addPartnerButton.setOnClickListener(view -> addPartnerFields());
        submitButton.setOnClickListener(view -> submitForm());

    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void addPartnerFields() {
        View partnerView = LayoutInflater.from(this).inflate(R.layout.activity_form_partner_fields, partnerContainer, false);
        Button removePartnerButton = partnerView.findViewById(R.id.removePartnerButton);
        Button selectPreferencesButton = partnerView.findViewById(R.id.partnerSelectSpecialPreferencesButton);

        selectPreferencesButton.setOnClickListener(v -> showSpecialPreferencesDialog(partnerView));
        removePartnerButton.setOnClickListener(view -> {
            partnerContainer.removeView(partnerView);
        });
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
            } else {
                partnerPreferencesMap.put(sourceView, new ArrayList<>(selectedPreferences));
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

    private List<TripConfiguration.TravelPartner> collectPartnersData() {
        List<TripConfiguration.TravelPartner> partners = new ArrayList<>();

        for (int i = 0; i < partnerContainer.getChildCount(); i++) {
            View partnerView = partnerContainer.getChildAt(i);

            EditText ageInput = partnerView.findViewById(R.id.partnerAgeInput);
            RadioGroup genderGroup = partnerView.findViewById(R.id.partnerGenderGroup);
            int selectedId = genderGroup.getCheckedRadioButtonId();
            RadioButton genderButton = partnerView.findViewById(selectedId);
            String gender = genderButton != null ? genderButton.getText().toString() : "";

            List<String> specialPreferences = getSpecialPreferencesForPartner(partnerView);

            int age = 0;
            try {
                age = Integer.parseInt(ageInput.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid age for all partners", Toast.LENGTH_SHORT).show();
                continue;
            }

            if (!gender.isEmpty()) {
                TripConfiguration.TravelPartner partner = new TripConfiguration.TravelPartner(age, gender, specialPreferences);
                partners.add(partner);
            }
        }

        return partners;
    }

    private void setupFormSubmission() {
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> submitForm());
    }

    private void submitForm() {
        EditText ageInput = findViewById(R.id.ageInput);
        String age = ageInput.getText().toString();

        RadioGroup genderGroup = findViewById(R.id.genderGroup);
        int selectedId = genderGroup.getCheckedRadioButtonId();
        RadioButton genderButton = findViewById(selectedId);
        String gender = (genderButton != null) ? genderButton.getText().toString() : "";

        List<String> preferences = new ArrayList<>(userSelectedItems);
        List<TripConfiguration.TravelPartner> partners = collectPartnersData();

        tripConfigurationViewModel.updateTripConfiguration(age, gender, preferences, partners);
        tripConfigurationViewModel.saveTripConfiguration();
    }
}
