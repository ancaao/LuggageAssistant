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
        Button purposeButton = findViewById(R.id.selectTravelPurposeButton);
        Button activitiesButton = findViewById(R.id.selectActivitiesButton);
        Button eventsButton = findViewById(R.id.selectSpecialEventsButton);

        populateSavedData();

        setupTravelPurposeSelection(purposeButton);
        setupPlannedActivitiesSelection(activitiesButton);
        setupSpecialEventsSelection(eventsButton);

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

    private void submitForm() {
        Button purposeButton = findViewById(R.id.selectTravelPurposeButton);
        Button activitiesButton = findViewById(R.id.selectActivitiesButton);

        TextView travelPurposeErrorText = findViewById(R.id.travelPurposeErrorText);
        TextView activitiesErrorText = findViewById(R.id.activitiesErrorText);

        boolean isValid = true;

        isValid &= com.example.luggageassistant.utils.InputValidator.isButtonSelectionValid(
                purposeButton, travelPurposeErrorText, "Select purpose"
        );

        isValid &= com.example.luggageassistant.utils.InputValidator.isButtonSelectionValid(
                activitiesButton, activitiesErrorText, "Select activities"
        );

        if (!isValid) {
            return;
        }

        // Daca toate câmpurile sunt valide, continuăm cu salvarea
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        Log.d("SubmitForm", "Submitting trip configuration: " + tripConfiguration.toMap().toString());

        TripConfigurationRepository.getInstance().saveTripConfiguration(tripConfiguration, new TripConfigurationRepository.OnDataSavedCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(StepFourActivity.this, "Trip configuration saved successfully!", Toast.LENGTH_SHORT).show();
                tripConfigurationViewModel.resetTripConfiguration();
                startActivity(new Intent(StepFourActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StepFourActivity.this, "Error saving trip configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupTravelPurposeSelection(Button purposeButton) {
        String[] options = {"Leisure", "Business", "Study", "Medical", "Other"};

        purposeButton.setOnClickListener(view -> {
            TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

            boolean[] checkedItems = new boolean[options.length];
            List<String> selected = new ArrayList<>();

            // Sincronizăm selected și checkedItems cu ce avem în ViewModel
            if (tripConfiguration.getTravelPurpose() != null) {
                selected.addAll(tripConfiguration.getTravelPurpose());

                for (int i = 0; i < options.length; i++) {
                    if (selected.contains(options[i])) {
                        checkedItems[i] = true;
                    }
                }
            }

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
                    purposeButton.setText("Select travel purpose");
                } else {
                    purposeButton.setText(String.join(", ", selected));
                }

                tripConfigurationViewModel.setTravelPurpose(new ArrayList<>(selected));
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupPlannedActivitiesSelection(Button activitiesButton) {
        String[] options = {"Hiking", "Museum visits", "Shopping", "Beach", "Nightlife"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        activitiesButton.setOnClickListener(view -> {
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
                    activitiesButton.setText("Select activities");
                } else {
                    activitiesButton.setText(String.join(", ", selected));
                    tripConfigurationViewModel.setPlannedActivities(new ArrayList<>(selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupSpecialEventsSelection(Button eventsButton) {
        String[] options = {"Wedding", "Festival", "Conference", "Birthday", "Anniversary"};
        boolean[] checkedItems = new boolean[options.length];
        List<String> selected = new ArrayList<>();

        eventsButton.setOnClickListener(view -> {
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
                    eventsButton.setText("Select events");
                } else {
                    eventsButton.setText(String.join(", ", selected));
                    tripConfigurationViewModel.setSpecialEvents(new ArrayList<>(selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }
    private void populateSavedData() {
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        Button purposeButton = findViewById(R.id.selectTravelPurposeButton);
        Button activitiesButton = findViewById(R.id.selectActivitiesButton);
        Button eventsButton = findViewById(R.id.selectSpecialEventsButton);

        if (tripConfiguration.getTravelPurpose() != null && !tripConfiguration.getTravelPurpose().isEmpty()) {
            purposeButton.setText(String.join(", ", tripConfiguration.getTravelPurpose()));
        }

        if (tripConfiguration.getPlannedActivities() != null && !tripConfiguration.getPlannedActivities().isEmpty()) {
            activitiesButton.setText(String.join(", ", tripConfiguration.getPlannedActivities()));
        }

        if (tripConfiguration.getSpecialEvents() != null && !tripConfiguration.getSpecialEvents().isEmpty()) {
            eventsButton.setText(String.join(", ", tripConfiguration.getSpecialEvents()));
        }
    }

}
