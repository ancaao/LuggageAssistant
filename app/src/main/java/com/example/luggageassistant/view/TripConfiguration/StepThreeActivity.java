package com.example.luggageassistant.view.TripConfiguration;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
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
    private TextInputEditText cityEditText;
    private TripConfigurationViewModel tripConfigurationViewModel;

    private final List<String> countryNames = new ArrayList<>();
    private final Map<String, String> countryCodeMap = new HashMap<>();
    private ArrayAdapter<String> countryAdapter;
    private TextInputEditText startDateInput, endDateInput;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private MaterialButton countrySelectorButton;


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

        populateSavedData();

        loadCountriesFromJson();

        countrySelectorButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StepThreeActivity.this);
            builder.setTitle("Select Country");
            builder.setItems(countryNames.toArray(new String[0]), (dialog, which) -> {
                String selected = countryNames.get(which);
                countrySelectorButton.setText(selected);
                tripConfigurationViewModel.setSelectedCountry(selected);
            });
            builder.show();
        });

        cityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tripConfigurationViewModel.getTripConfiguration().setCity(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        startDateInput.setOnClickListener(v -> showStartDatePicker(startCalendar, startDateInput));
        endDateInput.setOnClickListener(v -> showEndDatePicker(startCalendar, endCalendar, endDateInput));

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

            if (!isValid) {
                return;
            }

            // Variabilele cityEditText, startDateInput, endDateInput EXISTĂ deja
            String selectedCountry = countrySelectorButton.getText().toString();
            String enteredCity = cityEditText.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            tripConfigurationViewModel.updateFormStepThree(selectedCountry, enteredCity, startDate, endDate);

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
    private void showStartDatePicker(final Calendar startCalendar, final TextInputEditText startDateField) {
        MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Start Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        startDatePicker.addOnPositiveButtonClickListener(selection -> {
            startCalendar.setTimeInMillis(selection);
            updateLabel(startDateField, startCalendar);

            tripConfigurationViewModel.getTripConfiguration().setTripStartDate(startDateInput.getText().toString());
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

            tripConfigurationViewModel.getTripConfiguration().setTripEndDate(endDateInput.getText().toString());
        });

        endDatePicker.show(getSupportFragmentManager(), "END_DATE_PICKER");
    }

    private void updateLabel(TextInputEditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void populateSavedData() {
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        // Setăm country dacă există
        if (tripConfiguration.getCountry() != null && !tripConfiguration.getCountry().isEmpty()) {
            countrySelectorButton.setText(tripConfiguration.getCountry());
        }

        // Setăm city dacă există
        if (tripConfiguration.getCity() != null && !tripConfiguration.getCity().isEmpty()) {
            cityEditText.setText(tripConfiguration.getCity());
        }

        // Setăm start date dacă există
        if (tripConfiguration.getTripStartDate() != null && !tripConfiguration.getTripStartDate().isEmpty()) {
            startDateInput.setText(tripConfiguration.getTripStartDate());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                startCalendar.setTime(sdf.parse(tripConfiguration.getTripStartDate()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Setăm end date dacă există
        if (tripConfiguration.getTripEndDate() != null && !tripConfiguration.getTripEndDate().isEmpty()) {
            endDateInput.setText(tripConfiguration.getTripEndDate());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                endCalendar.setTime(sdf.parse(tripConfiguration.getTripEndDate()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
