package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.utils.MultiSelectDialog;
import com.example.luggageassistant.view.MainActivity;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import java.util.ArrayList;
import java.util.List;

public class StepFourActivity extends AppCompatActivity {
    private Button backButton, submitButton;
    private TripConfigurationViewModel tripConfigurationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_four);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        backButton = findViewById(R.id.stepFourBackButton);
        submitButton = findViewById(R.id.submitButton);
        TextView purposeText = findViewById(R.id.travelPurposeSelectedText);
        TextView activitiesText = findViewById(R.id.activitiesSelectedText);
        TextView eventsText = findViewById(R.id.specialEventsSelectedText);

        setupTravelPurposeSelection(purposeText);
        setupPlannedActivitiesSelection(activitiesText);
        setupSpecialEventsSelection(eventsText);

        View rootView = findViewById(android.R.id.content);
        setupTravelPurposeSelection(rootView);
        setupPlannedActivitiesSelection(rootView);
        setupSpecialEventsSelection(rootView);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void setupTravelPurposeSelection(View view) {
        TextView purposeText = view.findViewById(R.id.travelPurposeSelectedText);
        String[] options = {"Leisure", "Business", "Study", "Medical", "Other"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        purposeText.setOnClickListener(v -> MultiSelectDialog.showCustomMultiSelectDialog(
                this,
                "Select travel purpose",
                options,
                checkedItems,
                selected,
                purposeText
        ));
    }

    private void setupPlannedActivitiesSelection(View view) {
        TextView activitiesText = view.findViewById(R.id.activitiesSelectedText);
        String[] options = {"Hiking", "Museum visits", "Shopping", "Beach", "Nightlife"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        activitiesText.setOnClickListener(v -> MultiSelectDialog.showCustomMultiSelectDialog(
                this,
                "Select activities",
                options,
                checkedItems,
                selected,
                activitiesText
        ));
    }

    private void setupSpecialEventsSelection(View view) {
        TextView eventsText = view.findViewById(R.id.specialEventsSelectedText);
        String[] options = {"Wedding", "Festival", "Conference", "Birthday", "Anniversary"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        eventsText.setOnClickListener(v -> MultiSelectDialog.showCustomMultiSelectDialog(
                this,
                "Select special events",
                options,
                checkedItems,
                selected,
                eventsText
        ));

    }


    private void submitForm() {
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        Log.d("SubmitForm", "Submitting trip configuration: " + tripConfiguration.toMap().toString());

        TripConfigurationRepository.getInstance().saveTripConfiguration(tripConfigurationViewModel.getTripConfiguration(), new TripConfigurationRepository.OnDataSavedCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(StepFourActivity.this, "Trip configuration saved successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StepFourActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StepFourActivity.this, "Error saving trip configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTravelPurposeSelection(TextView purposeText) {
        String[] options = {"Leisure", "Business", "Study", "Medical", "Other"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        purposeText.setOnClickListener(view -> {
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 20, 50, 0);

            EditText otherInput = new EditText(this);
            otherInput.setHint("Add custom purpose...");
            otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogLayout.addView(otherInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select travel purpose");

            builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selected.contains(options[which])) {
                        selected.add(options[which]);
                    }
                } else {
                    selected.remove(options[which]);
                }
            });

            builder.setView(dialogLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String other = otherInput.getText().toString().trim();
                if (!other.isEmpty() && !selected.contains(other)) {
                    selected.add(other);
                }

                if (selected.isEmpty()) {
                    purposeText.setText("Select travel purpose");
                } else {
                    purposeText.setText(String.join(", ", selected));
                    tripConfigurationViewModel.setTravelPurpose(new ArrayList<>(selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupPlannedActivitiesSelection(TextView activitiesText) {
        String[] options = {"Hiking", "Museum visits", "Shopping", "Beach", "Nightlife"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        activitiesText.setOnClickListener(view -> {
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 20, 50, 0);

            EditText otherInput = new EditText(this);
            otherInput.setHint("Add custom activity...");
            otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogLayout.addView(otherInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select activities");

            builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selected.contains(options[which])) {
                        selected.add(options[which]);
                    }
                } else {
                    selected.remove(options[which]);
                }
            });

            builder.setView(dialogLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String other = otherInput.getText().toString().trim();
                if (!other.isEmpty() && !selected.contains(other)) {
                    selected.add(other);
                }

                if (selected.isEmpty()) {
                    activitiesText.setText("Select activities");
                } else {
                    activitiesText.setText(String.join(", ", selected));
                    tripConfigurationViewModel.setPlannedActivities(new ArrayList<>(selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupSpecialEventsSelection(TextView eventsText) {
        String[] options = {"Wedding", "Festival", "Conference", "Birthday", "Anniversary"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        eventsText.setOnClickListener(view -> {
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 20, 50, 0);

            EditText otherInput = new EditText(this);
            otherInput.setHint("Add custom event...");
            otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogLayout.addView(otherInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select special events");

            builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selected.contains(options[which])) {
                        selected.add(options[which]);
                    }
                } else {
                    selected.remove(options[which]);
                }
            });

            builder.setView(dialogLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String other = otherInput.getText().toString().trim();
                if (!other.isEmpty() && !selected.contains(other)) {
                    selected.add(other);
                }

                if (selected.isEmpty()) {
                    eventsText.setText("Select events");
                } else {
                    eventsText.setText(String.join(", ", selected));
                    tripConfigurationViewModel.setSpecialEvents(new ArrayList<>(selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }


}
