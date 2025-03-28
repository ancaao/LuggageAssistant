package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.os.Bundle;
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
import java.util.List;

public class StepOneActivity extends AppCompatActivity {
    private LinearLayout partnerContainer;
    private Button addPartnerButton, selectSpecialPreferencesButton, submitButton;
    private List<String> userSelectedItems = new ArrayList<>();
    private boolean[] checkedItems;
    private TripConfiguration tripConfiguration;
    private TripConfigurationViewModel tripConfigurationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_one);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        partnerContainer = findViewById(R.id.partnerContainer);
        addPartnerButton = findViewById(R.id.addPartnerButton);
        selectSpecialPreferencesButton = findViewById(R.id.selectSpecialPreferencesButton);
        submitButton = findViewById(R.id.submitButton);

        selectSpecialPreferencesButton.setOnClickListener(view -> showSpecialPreferencesDialog());
        addPartnerButton.setOnClickListener(view -> addPartnerFields());
        submitButton.setOnClickListener(view -> submitForm());
    }


    private void addPartnerFields() {
        View partnerView = LayoutInflater.from(this).inflate(R.layout.activity_form_partner_fields, partnerContainer, false);
        partnerContainer.addView(partnerView);
    }

    private void showSpecialPreferencesDialog() {
        String[] items = getResources().getStringArray(R.array.special_preferences);
        checkedItems = new boolean[items.length];

        EditText input = new EditText(this);
        input.setHint("Add new preference");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Preferences");
        builder.setView(input);
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
            String currentItem = items[which];
            if (isChecked) {
                userSelectedItems.add(currentItem);
            } else {
                userSelectedItems.remove(currentItem);
            }
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newItem = input.getText().toString().trim();
            if (!newItem.isEmpty()) {
                userSelectedItems.add(newItem);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private boolean validateInput() {
        // Implementează validarea aici
        return true;
    }

    private void submitForm() {
        // Aici se vor colecta toate datele și se vor procesa sau trimite unde este necesar
        Toast.makeText(this, "Submitting form...", Toast.LENGTH_SHORT).show();
        // Aici ai putea include logica de validare și trimitere a datelor la un server sau într-o bază de date locală
        // De exemplu, poți verifica dacă ageInput este completat corect, etc.
    }
}
