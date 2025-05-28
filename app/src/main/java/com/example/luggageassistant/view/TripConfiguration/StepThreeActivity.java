package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.Destination;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.utils.InputValidator;
import com.example.luggageassistant.utils.StepperUtils;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StepThreeActivity extends AppCompatActivity {

    private Button backButton, nextButton;
    private AutoCompleteTextView cityEditText;
    private TripConfigurationViewModel tripConfigurationViewModel;

    private final List<String> countryNames = new ArrayList<>();
    private final Map<String, String> countryCodeMap = new HashMap<>();
    private ArrayAdapter<String> countryAdapter;
    private TextInputEditText startDateInput, endDateInput;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private MaterialButton countrySelectorButton;
    private LinearLayout container;
    private MaterialButton addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_three);

        StepperUtils.configureStep(this, 3);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        backButton = findViewById(R.id.stepThreeBackButton);
        nextButton = findViewById(R.id.stepThreeNextButton);
        cityEditText = findViewById(R.id.citySpinner);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        countrySelectorButton = findViewById(R.id.countrySelectorButton);
        container = findViewById(R.id.destinationContainer);
        addButton = findViewById(R.id.addDestinationButton);

        populateSavedData();

        loadCountriesFromJson();

        countrySelectorButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StepThreeActivity.this);
            builder.setTitle("Select Country");
            builder.setItems(countryNames.toArray(new String[0]), (dialog, which) -> {
                String selected = countryNames.get(which);
                countrySelectorButton.setText(selected);
                tripConfigurationViewModel.setSelectedCountry(selected);
                Log.d("SELECTED_COUNTRY", "[" + selected + "]");

                loadCitiesForSelectedCountry(selected);
            });
            builder.show();
        });

        startDateInput.setOnClickListener(v -> showStartDatePicker(startCalendar, startDateInput));
        endDateInput.setOnClickListener(v -> showEndDatePicker(startCalendar, endCalendar, endDateInput));

        addButton.setOnClickListener(v -> {
            addDestinationView(container, null);
        });

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(view -> {
            TextInputLayout cityInputLayout = findViewById(R.id.cityInputLayout);
            TextInputLayout startDateInputLayout = findViewById(R.id.startDateInputLayout);
            TextInputLayout endDateInputLayout = findViewById(R.id.endDateInputLayout);
            TextView countryErrorText = findViewById(R.id.countryErrorText);

            boolean isValid = true;

            isValid &= InputValidator.isCountrySelected(countrySelectorButton, countryErrorText);
            isValid &= InputValidator.isFieldNotEmpty(cityInputLayout);
            isValid &= InputValidator.isFieldNotEmpty(startDateInputLayout);
            isValid &= InputValidator.isFieldNotEmpty(endDateInputLayout);

            // ✅ Verificăm că orașul a fost selectat din listă
            ArrayAdapter<String> cityAdapter = (ArrayAdapter<String>) cityEditText.getAdapter();
            if (cityAdapter == null || cityAdapter.getPosition(cityEditText.getText().toString()) < 0) {
                cityInputLayout.setError("Please select a valid city from the list");
                isValid = false;
            } else {
                cityInputLayout.setError(null); // Curățăm eroarea dacă e valid
            }

            if (!isValid) return;

            tripConfigurationViewModel.getDestinations().getValue().clear();

            // ✅ Salvăm prima destinație (statică)
            Destination first = new Destination(
                    countrySelectorButton.getText().toString(),
                    cityEditText.getText().toString().trim(),
                    startDateInput.getText().toString().trim(),
                    endDateInput.getText().toString().trim()
            );
            tripConfigurationViewModel.addDestination(first);

            // ✅ Salvăm destinațiile dinamice
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                MaterialButton countryBtn = child.findViewById(R.id.countrySelectorButton);
                AutoCompleteTextView city = child.findViewById(R.id.citySpinner);
                TextInputEditText start = child.findViewById(R.id.startDateInput);
                TextInputEditText end = child.findViewById(R.id.endDateInput);

                if (countryBtn != null && city != null && start != null && end != null) {
                    Destination destination = new Destination(
                            countryBtn.getText().toString(),
                            city.getText().toString().trim(),
                            start.getText().toString().trim(),
                            end.getText().toString().trim()
                    );
                    tripConfigurationViewModel.addDestination(destination);
                }
            }

            Intent intent = new Intent(this, StepFourActivity.class);
            startActivity(intent);
        });

    }

    private void loadCountriesFromJson() {
        try {
            InputStream is = getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject country = jsonArray.getJSONObject(i);
                String name = country.getString("name");
                String code = country.getString("code");

                countryNames.add(name);
                countryCodeMap.put(name, code);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading countries", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCitiesForSelectedCountry(String selectedCountry) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cities")
                .whereEqualTo("country", selectedCountry)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> cityList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        cityList.add(doc.getString("city"));
                    }
                    tripConfigurationViewModel.setCachedCities(cityList);
                    updateCityDropdown(cityList);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to load cities", e));
    }

    private void updateCityDropdown(List<String> cityList) {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
        cityEditText.setAdapter(cityAdapter);
    }

    private void showStartDatePicker(final Calendar startCalendar, final TextInputEditText startDateField) {
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            startCalendar.setTimeInMillis(selection);
            updateLabel(startDateField, startCalendar);

//            tripConfigurationViewModel.getTripConfiguration().setTripStartDate(startDateInput.getText().toString());
        });

        startDatePicker.show(getSupportFragmentManager(), "START_DATE_PICKER");
    }

    private void showEndDatePicker(final Calendar startCalendar, final Calendar endCalendar, final TextInputEditText endDateField) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(startCalendar.getTimeInMillis()));

        MaterialDatePicker<Long> endDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select End Date")
                .setSelection(startCalendar.getTimeInMillis()) // EndDate default = StartDate
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            endCalendar.setTimeInMillis(selection);
            updateLabel(endDateField, endCalendar);

//            tripConfigurationViewModel.getTripConfiguration().setTripEndDate(endDateInput.getText().toString());
        });

        endDatePicker.show(getSupportFragmentManager(), "END_DATE_PICKER");
    }

    private void addDestinationView(LinearLayout container, @Nullable Destination existing) {
        View destinationView = LayoutInflater.from(this).inflate(R.layout.activity_form_destination_fields, container, false);

        MaterialButton countryBtn = destinationView.findViewById(R.id.dynamicCountrySelectorButton);
        AutoCompleteTextView citySpinner = destinationView.findViewById(R.id.dynamicCitySpinner);
        TextInputEditText startInput = destinationView.findViewById(R.id.startDateInput);
        TextInputEditText endInput = destinationView.findViewById(R.id.endDateInput);
        MaterialButton deleteBtn = destinationView.findViewById(R.id.removeDestinationButton);

        Calendar localStartCalendar = Calendar.getInstance();
        Calendar localEndCalendar = Calendar.getInstance();

        countryBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StepThreeActivity.this);
            builder.setTitle("Select Country");
            builder.setItems(countryNames.toArray(new String[0]), (dialog, which) -> {
                String selected = countryNames.get(which);
                countryBtn.setText(selected);
                loadCitiesForDynamicCountry(selected, citySpinner);
            });
            builder.show();
        });

        startInput.setOnClickListener(v -> showStartDatePicker(localStartCalendar, startInput));
        endInput.setOnClickListener(v -> showEndDatePicker(localStartCalendar, localEndCalendar, endInput));

        deleteBtn.setOnClickListener(v -> container.removeView(destinationView));

        // Dacă avem un obiect existent, populăm câmpurile
        if (existing != null) {
            countryBtn.setText(existing.getCountry());
            citySpinner.setText(existing.getCity());
            startInput.setText(existing.getTripStartDate());
            endInput.setText(existing.getTripEndDate());

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                localStartCalendar.setTime(sdf.parse(existing.getTripStartDate()));
                localEndCalendar.setTime(sdf.parse(existing.getTripEndDate()));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            loadCitiesForDynamicCountry(existing.getCountry(), citySpinner);
        }

        container.addView(destinationView);
    }

    private void loadCitiesForDynamicCountry(String selectedCountry, AutoCompleteTextView cityField) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cities")
                .whereEqualTo("country", selectedCountry)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> cityList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        cityList.add(doc.getString("city"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
                    cityField.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to load cities", e));
    }


    private void updateLabel(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void populateSavedData() {
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();
        List<Destination> savedDestinations = tripConfiguration.getDestinations();

        if (savedDestinations != null && !savedDestinations.isEmpty()) {
            Destination firstDestination = savedDestinations.get(0);

            // Setăm country dacă există
            if (firstDestination.getCountry() != null && !firstDestination.getCountry().isEmpty()) {
                countrySelectorButton.setText(firstDestination.getCountry());
            }

            // Setăm city dacă există
            if (firstDestination.getCity() != null && !firstDestination.getCity().isEmpty()) {
                cityEditText.setText(firstDestination.getCity());
            }

            // Setăm start date dacă există
            if (firstDestination.getTripStartDate() != null && !firstDestination.getTripStartDate().isEmpty()) {
                startDateInput.setText(firstDestination.getTripStartDate());
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    startCalendar.setTime(sdf.parse(firstDestination.getTripStartDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Setăm end date dacă există
            if (firstDestination.getTripEndDate() != null && !firstDestination.getTripEndDate().isEmpty()) {
                endDateInput.setText(firstDestination.getTripEndDate());
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    endCalendar.setTime(sdf.parse(firstDestination.getTripEndDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Afișăm și celelalte destinații (dacă sunt)
        if (savedDestinations != null && savedDestinations.size() > 1) {
            for (int i = 1; i < savedDestinations.size(); i++) {
                addDestinationView(container, savedDestinations.get(i));
            }
        }
    }

}