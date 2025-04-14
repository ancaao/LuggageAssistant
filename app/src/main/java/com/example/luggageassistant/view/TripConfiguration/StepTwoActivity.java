package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Luggage;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.view.MainActivity;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import java.util.ArrayList;
import java.util.List;

public class StepTwoActivity extends AppCompatActivity {
    private Button backButton, nextButton;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView ownerSelectedText;
    private List<String> allNames = new ArrayList<>();
    private List<String> selectedOwners = new ArrayList<>();
    private final List<String> selectedAccessories = new ArrayList<>();
    private LinearLayout luggageContainer;
    private Button addLuggageButton;
    private List<View> luggageViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_two);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        Spinner luggageTypeSpinner = findViewById(R.id.luggageTypeSpinner);
        backButton = findViewById(R.id.stepTwoBackButton);
        nextButton = findViewById(R.id.stepTwoNextButton);
        ownerSelectedText = findViewById(R.id.ownerSelectedText);
        luggageContainer = findViewById(R.id.luggageContainer);
        addLuggageButton = findViewById(R.id.addLuggageButton);

        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        if (tripConfiguration.getName() != null) {
            allNames.add(tripConfiguration.getName());
        }
        if (tripConfiguration.getPartner() != null) {
            for (TravelPartner partner : tripConfiguration.getPartner()) {
                allNames.add(partner.getName());
            }
        }

        String[] namesArray = allNames.toArray(new String[0]);
        boolean[] checkedItems = new boolean[namesArray.length];

        setupOwnerSelection(ownerSelectedText, namesArray, checkedItems);

        setupLuggageTypeSpinner(luggageTypeSpinner);
        String selectedLuggageType = luggageTypeSpinner.getSelectedItem().toString();

        TextView accessoriesTextView = findViewById(R.id.accessoriesSelectedText);
        setupAccessoriesSelection(accessoriesTextView);

        addLuggageButton.setOnClickListener(view -> addLuggageFields());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nextButton.setOnClickListener(view -> {
            List<Luggage> luggages = collectLuggageData();
            tripConfigurationViewModel.updateFormStepTwo(luggages);

            Intent intent = new Intent(this, StepThreeActivity.class);
            startActivity(intent);
        });

    }

    private void setupAccessoriesSelection(TextView accessoriesTextView) {
        String[] accessoriesOptions = {
                "Baby equipment",
                "Musical instruments",
                "Sports equipment",
                "Mobility equipment"
        };
        boolean[] checkedItems = new boolean[accessoriesOptions.length];

        accessoriesTextView.setOnClickListener(view -> {
            // Creează layout custom pentru EditText "altele"
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 20, 50, 0);

            EditText otherInput = new EditText(this);
            otherInput.setHint("Add new accessory...");
            otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogLayout.addView(otherInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(StepTwoActivity.this);
            builder.setTitle("Select accessories");
            builder.setMultiChoiceItems(accessoriesOptions, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selectedAccessories.contains(accessoriesOptions[which])) {
                        selectedAccessories.add(accessoriesOptions[which]);
                    }
                } else {
                    selectedAccessories.remove(accessoriesOptions[which]);
                }
            });

            builder.setView(dialogLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String other = otherInput.getText().toString().trim();
                if (!other.isEmpty() && !selectedAccessories.contains(other)) {
                    selectedAccessories.add(other);
                }

                if (selectedAccessories.isEmpty()) {
                    accessoriesTextView.setText("Select accessories");
                } else {
                    accessoriesTextView.setText(String.join(", ", selectedAccessories));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }


    private void setupOwnerSelection(TextView ownerTextView, String[] namesArray, boolean[] checkedItems) {
        ownerTextView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StepTwoActivity.this);
            builder.setTitle("Select owner(s)");

            builder.setMultiChoiceItems(namesArray, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selectedOwners.contains(namesArray[which])) {
                        selectedOwners.add(namesArray[which]);
                    }
                } else {
                    selectedOwners.remove(namesArray[which]);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                if (selectedOwners.isEmpty()) {
                    ownerTextView.setText("Select owner(s)");
                } else {
                    ownerTextView.setText(String.join(", ", selectedOwners));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void addLuggageFields() {
        View luggageView = LayoutInflater.from(this).inflate(R.layout.activity_form_luggage_fields, luggageContainer, false);

        Button removeButton = luggageView.findViewById(R.id.removePartnerButton);
        removeButton.setText("Remove luggage (-)");
        removeButton.setOnClickListener(v -> {
            luggageContainer.removeView(luggageView);
            luggageViews.remove(luggageView);
        });

        Spinner luggageTypeSpinner = luggageView.findViewById(R.id.luggageTypeSpinner);
        setupLuggageTypeSpinner(luggageTypeSpinner);

        String[] allNamesArray = allNames.toArray(new String[0]);
        setupOwnerSelectionForLuggage(luggageView, allNamesArray);
        setupAccessoriesSelectionForLuggage(luggageView);

        luggageContainer.addView(luggageView);
        luggageViews.add(luggageView);
    }


    private void setupLuggageTypeSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.luggage_type_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void setupOwnerSelectionForLuggage(View luggageView, String[] allNames) {
        TextView ownerText = luggageView.findViewById(R.id.ownerSelectedText);
        boolean[] checkedItems = new boolean[allNames.length];
        List<String> selected = new ArrayList<>();

        ownerText.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select owner(s)");

            builder.setMultiChoiceItems(allNames, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selected.contains(allNames[which])) {
                        selected.add(allNames[which]);
                    }
                } else {
                    selected.remove(allNames[which]);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                if (selected.isEmpty()) {
                    ownerText.setText("Select owner(s)");
                } else {
                    ownerText.setText(String.join(", ", selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }
    private void setupAccessoriesSelectionForLuggage(View luggageView) {
        TextView accessoriesText = luggageView.findViewById(R.id.accessoriesSelectedText);
        String[] accessoriesOptions = {
                "Baby equipment",
                "Musical instruments",
                "Sports equipment",
                "Mobility equipment"
        };
        boolean[] checkedItems = new boolean[accessoriesOptions.length];
        List<String> selected = new ArrayList<>();

        accessoriesText.setOnClickListener(view -> {
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.VERTICAL);
            dialogLayout.setPadding(50, 20, 50, 0);

            EditText otherInput = new EditText(this);
            otherInput.setHint("Add new accessory...");
            otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
            dialogLayout.addView(otherInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select accessories");
            builder.setMultiChoiceItems(accessoriesOptions, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    if (!selected.contains(accessoriesOptions[which])) {
                        selected.add(accessoriesOptions[which]);
                    }
                } else {
                    selected.remove(accessoriesOptions[which]);
                }
            });

            builder.setView(dialogLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String other = otherInput.getText().toString().trim();
                if (!other.isEmpty() && !selected.contains(other)) {
                    selected.add(other);
                }

                if (selected.isEmpty()) {
                    accessoriesText.setText("Select accessories");
                } else {
                    accessoriesText.setText(String.join(", ", selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private List<Luggage> collectLuggageData() {
        List<Luggage> luggages = new ArrayList<>();

        for (View luggageView : luggageViews) {
            Spinner typeSpinner = luggageView.findViewById(R.id.luggageTypeSpinner);
            EditText lengthInput = luggageView.findViewById(R.id.lengthInput);
            EditText widthInput = luggageView.findViewById(R.id.widthInput);
            EditText heightInput = luggageView.findViewById(R.id.heightInput);
            EditText weightInput = luggageView.findViewById(R.id.weightInput);
            TextView accessoriesText = luggageView.findViewById(R.id.accessoriesSelectedText);
            TextView ownerText = luggageView.findViewById(R.id.ownerSelectedText);

            List<String> owners = parseListFromText(ownerText.getText().toString());
            String type = typeSpinner.getSelectedItem().toString();
            int length = parseInteger(lengthInput.getText().toString());
            int width = parseInteger(widthInput.getText().toString());
            int height = parseInteger(heightInput.getText().toString());
            int weight = parseInteger(weightInput.getText().toString());
            List<String> accessories = parseListFromText(accessoriesText.getText().toString());

            Luggage luggage = new Luggage(owners, type, length, width, height, weight, accessories);
            luggages.add(luggage);
        }

        return luggages;
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<String> parseListFromText(String text) {
        if (text.equals("Select accessories") || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(text.split("\\s*,\\s*")));
    }

}
