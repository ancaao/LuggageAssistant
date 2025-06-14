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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Luggage;
import com.example.luggageassistant.model.TravelPartner;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.utils.InputValidator;
import com.example.luggageassistant.utils.StepperUtils;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.textfield.TextInputLayout;

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

        StepperUtils.configureStep(this, 2);

        TextView selectOwnersErrorText = findViewById(R.id.selectOwnersErrorText);
        TextView luggageTypeErrorText = findViewById(R.id.luggageTypeErrorText);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        Button luggageTypeButton = findViewById(R.id.luggageTypeButton);
        backButton = findViewById(R.id.stepTwoBackButton);
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

        populateSavedData();

        setupOwnerSelection(selectOwnersButton);

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

            TextInputLayout lengthLayout = findViewById(R.id.lengthInputLayout);
            TextInputLayout widthLayout = findViewById(R.id.widthInputLayout);
            TextInputLayout heightLayout = findViewById(R.id.heightInputLayout);
            TextInputLayout weightLayout = findViewById(R.id.weightInputLayout);

            isValid &= InputValidator.isDimensionValid(lengthLayout, 200, "Length");
            isValid &= InputValidator.isDimensionValid(widthLayout, 200, "Width");
            isValid &= InputValidator.isDimensionValid(heightLayout, 200, "Height");
            isValid &= InputValidator.isDimensionValid(weightLayout, 50, "Weight");

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

                TextInputLayout dynLengthLayout = luggageView.findViewById(R.id.lengthInputLayout);
                TextInputLayout dynWidthLayout = luggageView.findViewById(R.id.widthInputLayout);
                TextInputLayout dynHeightLayout = luggageView.findViewById(R.id.heightInputLayout);
                TextInputLayout dynWeightLayout = luggageView.findViewById(R.id.weightInputLayout);

                isValid &= InputValidator.isDimensionValid(dynLengthLayout, 200, "Length");
                isValid &= InputValidator.isDimensionValid(dynWidthLayout, 200, "Width");
                isValid &= InputValidator.isDimensionValid(dynHeightLayout, 200, "Height");
                isValid &= InputValidator.isDimensionValid(dynWeightLayout, 50, "Weight");

                Button ownerButton = luggageView.findViewById(R.id.ownerSelectedText);
                Button typeButton = luggageView.findViewById(R.id.luggageTypeSpinner);

                List<String> owners = parseListFromText(ownerButton.getText().toString());
                String type = typeButton.getText().toString();

                TextView ownersErrorText = luggageView.findViewById(R.id.extraLuggageSelectOwnersErrorText);
                TextView typeErrorText = luggageView.findViewById(R.id.extraLuggageLuggageTypeErrorText);

                if (ownersErrorText == null || typeErrorText == null) {
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

            List<Luggage> luggages = new ArrayList<>();

            // 1. Adaugă manual bagajul principal
            Button typeButton = findViewById(R.id.luggageTypeButton);
            Button ownerButton = findViewById(R.id.selectOwnersButton);

            List<String> owners = new ArrayList<>(selectedOwners);
            String type = typeButton.getText().toString();
            List<String> accessories = new ArrayList<>(selectedAccessories);

            Luggage mainLuggage = new Luggage(owners, type, 0, 0, 0, 0, accessories);
            luggages.add(mainLuggage);

            // 2. Adaugă bagajele dinamice
            luggages.addAll(collectLuggageData());

            // 3. Update corect ViewModel-ul
            tripConfigurationViewModel.getTripConfiguration().setLuggage(luggages);

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
                saveCurrentLuggages();
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void setupOwnerSelection(Button ownerButton) {
        ownerButton.setOnClickListener(view -> {
            // 🧠 🔁 FIX: reconstruim lista actuală de nume
            TripConfiguration config = tripConfigurationViewModel.getTripConfiguration();
            List<String> currentNames = new ArrayList<>();
            if (config.getName() != null) currentNames.add(config.getName());
            if (config.getPartner() != null) {
                for (TravelPartner partner : config.getPartner()) {
                    if (partner.getName() != null) currentNames.add(partner.getName());
                }
            }

            // 🧹 eliminăm selecțiile invalide
            selectedOwners.retainAll(currentNames);

            // 🧩 reconstruim namesArray și checkedItems
            final String[] namesArray = currentNames.toArray(new String[0]);
            final boolean[] checkedItems = new boolean[namesArray.length];
            for (int i = 0; i < namesArray.length; i++) {
                checkedItems[i] = selectedOwners.contains(namesArray[i]);
            }

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
                saveCurrentLuggages();
            });

            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        });
    }

    private void addLuggageFields() {
        View luggageView = LayoutInflater.from(this).inflate(R.layout.activity_form_luggage_fields, luggageContainer, false);

        Button removeButton = luggageView.findViewById(R.id.removeLuggageButton);
        removeButton.setText("Remove luggage (-)");
        removeButton.setOnClickListener(v -> {
            luggageContainer.removeView(luggageView);
            luggageViews.remove(luggageView);
            saveCurrentLuggages();
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

        EditText lengthInput = luggageView.findViewById(R.id.lengthInput);
        EditText widthInput = luggageView.findViewById(R.id.widthInput);
        EditText heightInput = luggageView.findViewById(R.id.heightInput);
        EditText weightInput = luggageView.findViewById(R.id.weightInput);

        // La fiecare focus lost => salvăm ViewModel
        lengthInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveCurrentLuggages();
        });
        widthInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveCurrentLuggages();
        });
        heightInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveCurrentLuggages();
        });
        weightInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveCurrentLuggages();
        });

        luggageContainer.addView(luggageView);
        luggageViews.add(luggageView);
        saveCurrentLuggages();
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
                saveCurrentLuggages();
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
                saveCurrentLuggages();
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
        if (text.equals("Select owner(s)") || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(text.split("\\s*,\\s*")));
    }

    private void saveCurrentLuggages() {
        List<Luggage> currentLuggages = new ArrayList<>();

        // 1. Salvăm bagajul principal
        Button typeButton = findViewById(R.id.luggageTypeButton);
        Button ownerButton = findViewById(R.id.selectOwnersButton);
        TextView accessoriesSummary = findViewById(R.id.accessoriesSelectedText);

        List<String> owners = new ArrayList<>(selectedOwners);
        String type = typeButton.getText().toString();
        List<String> accessories = new ArrayList<>(selectedAccessories);

        Luggage mainLuggage = new Luggage(owners, type, 0, 0, 0, 0, accessories);
        currentLuggages.add(mainLuggage);

        // 2. Salvăm bagajele dinamice
        currentLuggages.addAll(collectLuggageData());

        tripConfigurationViewModel.getTripConfiguration().setLuggage(currentLuggages);
    }

    private void populateSavedData() {
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();
        List<Luggage> savedLuggages = tripConfiguration.getLuggage();

        // 1. Golim containerele ca să nu duplicăm
        luggageContainer.removeAllViews();
        luggageViews.clear();

        if (savedLuggages != null && !savedLuggages.isEmpty()) {
            // 2. Primul luggage e pentru bagajul principal
            Luggage mainLuggage = savedLuggages.get(0);

            Button ownerButton = findViewById(R.id.selectOwnersButton);
            Button typeButton = findViewById(R.id.luggageTypeButton);
            TextView accessoriesSummary = findViewById(R.id.accessoriesSelectedText);

            if (mainLuggage.getOwners() != null && !mainLuggage.getOwners().isEmpty()) {
                selectedOwners = new ArrayList<>(mainLuggage.getOwners());
                ownerButton.setText(String.join(", ", selectedOwners));
            }

            if (mainLuggage.getLuggageType() != null) {
                typeButton.setText(mainLuggage.getLuggageType());
            }

            if (mainLuggage.getSpecialAccessories() != null && !mainLuggage.getSpecialAccessories().isEmpty()) {
                selectedAccessories.clear();
                selectedAccessories.addAll(mainLuggage.getSpecialAccessories());
                accessoriesSummary.setText(String.join(", ", selectedAccessories));
            }

            // 3. Restul luggage-urilor sunt cele dinamice
            for (int i = 1; i < savedLuggages.size(); i++) {
                Luggage luggage = savedLuggages.get(i);

                View luggageView = LayoutInflater.from(this).inflate(R.layout.activity_form_luggage_fields, luggageContainer, false);

                Button extraOwnerButton = luggageView.findViewById(R.id.ownerSelectedText);
                Button extraTypeButton = luggageView.findViewById(R.id.luggageTypeSpinner);
                EditText lengthInput = luggageView.findViewById(R.id.lengthInput);
                EditText widthInput = luggageView.findViewById(R.id.widthInput);
                EditText heightInput = luggageView.findViewById(R.id.heightInput);
                EditText weightInput = luggageView.findViewById(R.id.weightInput);
                TextView accessoriesSummaryExtra = luggageView.findViewById(R.id.accessoriesSummary);
                Button extraAccessoriesButton = luggageView.findViewById(R.id.accessoriesSelectedText);

                extraOwnerButton.setText(String.join(", ", luggage.getOwners()));
                extraTypeButton.setText(luggage.getLuggageType());
                lengthInput.setText(luggage.getLength() == 0 ? "" : String.valueOf(luggage.getLength()));
                widthInput.setText(luggage.getWidth() == 0 ? "" : String.valueOf(luggage.getWidth()));
                heightInput.setText(luggage.getHeight() == 0 ? "" : String.valueOf(luggage.getHeight()));
                weightInput.setText(luggage.getWeight() == 0 ? "" : String.valueOf(luggage.getWeight()));
                accessoriesSummaryExtra.setText(luggage.getSpecialAccessories() != null ? String.join(", ", luggage.getSpecialAccessories()) : "No accessories selected");

                // Setăm listener-ele
                String[] allNamesArray = allNames.toArray(new String[0]);
                setupOwnerSelectionForLuggageButton(extraOwnerButton, allNamesArray);
                setupLuggageTypeDropdownForLuggage(extraTypeButton);
                setupAccessoriesSelection(extraAccessoriesButton, accessoriesSummaryExtra);

                Button removeButton = luggageView.findViewById(R.id.removeLuggageButton);
                removeButton.setText("Remove luggage");
                removeButton.setOnClickListener(v -> {
                    luggageContainer.removeView(luggageView);
                    luggageViews.remove(luggageView);
                    saveCurrentLuggages();
                });

                luggageContainer.addView(luggageView);
                luggageViews.add(luggageView);
            }
        }
    }
}