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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Luggage;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import java.util.ArrayList;
import java.util.List;

public class StepTwoActivity extends AppCompatActivity {
    private Button backButton, nextButton, selectOwnersButton;
    private TripConfigurationViewModel tripConfigurationViewModel;
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
        TextView selectOwnersErrorText = findViewById(R.id.selectOwnersErrorText);
        TextView luggageTypeErrorText = findViewById(R.id.luggageTypeErrorText);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        Button luggageTypeButton = findViewById(R.id.luggageTypeButton);        backButton = findViewById(R.id.stepTwoBackButton);
        nextButton = findViewById(R.id.stepTwoNextButton);
        selectOwnersButton = findViewById(R.id.selectOwnersButton);
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

        setupOwnerSelection(selectOwnersButton, namesArray, checkedItems);

        String[] luggageTypes = getResources().getStringArray(R.array.luggage_type_options);

        luggageTypeButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Luggage Type");

            builder.setItems(luggageTypes, (dialog, which) -> {
                luggageTypeButton.setText(luggageTypes[which]);
            });

            builder.show();
        });

        Button accessoriesButton = findViewById(R.id.selectSpecialAccessoriesButton);
        TextView accessoriesSummary = findViewById(R.id.accessoriesSelectedText);
        setupAccessoriesSelection(accessoriesButton, accessoriesSummary);

        addLuggageButton.setOnClickListener(view -> addLuggageFields());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nextButton.setOnClickListener(view -> {
            boolean isValid = true;

            // Verificăm owners selectați (minim unul)
            if (selectedOwners.isEmpty()) {
                selectOwnersErrorText.setText("Please select at least one owner");
                selectOwnersErrorText.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                selectOwnersErrorText.setText("");
                selectOwnersErrorText.setVisibility(View.GONE);
            }

            // Verificăm fiecare bagaj
            for (View luggageView : luggageViews) {
                Button ownerButton = luggageView.findViewById(R.id.ownerSelectedText);
                Button typeButton = luggageView.findViewById(R.id.luggageTypeSpinner);

                List<String> owners = parseListFromText(ownerButton.getText().toString());
                String type = typeButton.getText().toString();

                TextView ownersErrorText = luggageView.findViewById(R.id.extraLuggageSelectOwnersErrorText);
                TextView typeErrorText = luggageView.findViewById(R.id.extraLuggageLuggageTypeErrorText);

                if (ownersErrorText == null || typeErrorText == null) {
                    Log.e("VALIDATION", "Error text views not found for a luggageView");
                    Toast.makeText(this, "Eroare la validare bagaj dinamic", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    continue;
                }

                if (owners.isEmpty()) {
                    ownersErrorText.setText("Please select at least one owner");
                    ownersErrorText.setVisibility(View.VISIBLE);
                    isValid = false;
                } else {
                    ownersErrorText.setText("");
                    ownersErrorText.setVisibility(View.GONE);
                }

                if (type.equals("Select Luggage Type") || type.trim().isEmpty()) {
                    typeErrorText.setText("Please select a luggage type");
                    typeErrorText.setVisibility(View.VISIBLE);
                    isValid = false;
                } else {
                    typeErrorText.setText("");
                    typeErrorText.setVisibility(View.GONE);
                }

            }

            // Validare pentru bagajul principal
            Button mainLuggageType = findViewById(R.id.luggageTypeButton);
            if (mainLuggageType.getText().toString().equals("Luggage type")) {
                luggageTypeErrorText.setText("Please select a luggage type");
                luggageTypeErrorText.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                luggageTypeErrorText.setText("");
                luggageTypeErrorText.setVisibility(View.GONE);
            }

            if (!isValid) return;

            List<Luggage> luggages = collectLuggageData();
            tripConfigurationViewModel.updateFormStepTwo(luggages);

            Intent intent = new Intent(this, StepThreeActivity.class);
            startActivity(intent);
        });
    }

    private void setupAccessoriesSelection(Button triggerButton, TextView summaryTextView) {
        String[] accessoriesOptions = {
                "Baby equipment",
                "Musical instruments",
                "Sports equipment",
                "Mobility equipment"
        };
        boolean[] checkedItems = new boolean[accessoriesOptions.length];

        triggerButton.setOnClickListener(view -> {
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
                    summaryTextView.setText("No accessories selected");
                } else {
                    summaryTextView.setText(String.join(", ", selectedAccessories));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupOwnerSelection(Button ownerButton, String[] namesArray, boolean[] checkedItems) {
        ownerButton.setOnClickListener(view -> {
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
                    ownerButton.setText("Select owner(s)");
                } else {
                    ownerButton.setText(String.join(", ", selectedOwners));
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

        // Owner(s)
        Button ownerButton = luggageView.findViewById(R.id.ownerSelectedText);
        String[] allNamesArray = allNames.toArray(new String[0]);
        setupOwnerSelectionForLuggageButton(ownerButton, allNamesArray);

        // Luggage type
        Button luggageTypeButton = luggageView.findViewById(R.id.luggageTypeSpinner);
        luggageTypeButton.setText("Select Luggage Type");
        setupLuggageTypeDropdownForLuggage(luggageTypeButton);

        // Accessories
        Button accessoriesButton = luggageView.findViewById(R.id.accessoriesSelectedText);
        TextView accessoriesSummary = luggageView.findViewById(R.id.accessoriesSummary);
        setupAccessoriesSelection(accessoriesButton, accessoriesSummary);

        luggageContainer.addView(luggageView);
        luggageViews.add(luggageView);
    }

    private void setupOwnerSelectionForLuggageButton(Button ownerButton, String[] allNames) {
        boolean[] checkedItems = new boolean[allNames.length];
        List<String> selected = new ArrayList<>();

        ownerButton.setOnClickListener(view -> {
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
                    ownerButton.setText("Select owner(s)");
                } else {
                    ownerButton.setText(String.join(", ", selected));
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupLuggageTypeDropdownForLuggage(Button luggageTypeButton) {
        String[] luggageTypes = getResources().getStringArray(R.array.luggage_type_options);

        luggageTypeButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Luggage Type");

            builder.setItems(luggageTypes, (dialog, which) -> {
                luggageTypeButton.setText(luggageTypes[which]);
            });

            builder.show();
        });
    }

    private List<Luggage> collectLuggageData() {
        List<Luggage> luggages = new ArrayList<>();

        for (View luggageView : luggageViews) {
            Button typeButton = luggageView.findViewById(R.id.luggageTypeSpinner);
            EditText lengthInput = luggageView.findViewById(R.id.lengthInput);
            EditText widthInput = luggageView.findViewById(R.id.widthInput);
            EditText heightInput = luggageView.findViewById(R.id.heightInput);
            EditText weightInput = luggageView.findViewById(R.id.weightInput);
            TextView accessoriesText = luggageView.findViewById(R.id.accessoriesSelectedText);
            TextView ownerText = luggageView.findViewById(R.id.ownerSelectedText);

            List<String> owners = parseListFromText(ownerText.getText().toString());
            String type = typeButton.getText().toString();
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
