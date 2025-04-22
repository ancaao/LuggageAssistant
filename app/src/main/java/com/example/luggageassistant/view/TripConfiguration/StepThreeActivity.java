package com.example.luggageassistant.view.TripConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

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
    private Spinner countrySpinner;
    private EditText cityEditText;
    private TripConfigurationViewModel tripConfigurationViewModel;

    private final List<String> countryNames = new ArrayList<>();
    private final Map<String, String> countryCodeMap = new HashMap<>();
    private ArrayAdapter<String> countryAdapter;
    private EditText startDateInput, endDateInput;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_three);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        backButton = findViewById(R.id.stepThreeBackButton);
        nextButton = findViewById(R.id.stepThreeNextButton);
        countrySpinner = findViewById(R.id.countrySpinner);
        cityEditText = findViewById(R.id.citySpinner);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);

        loadCountriesFromJson();

        startDateInput.setOnClickListener(v -> showDatePicker(startCalendar, startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endCalendar, endDateInput));

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(view -> {
            String selectedCountry = countrySpinner.getSelectedItem().toString();
            String enteredCity = cityEditText.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (enteredCity.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

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

            populateCountrySpinner();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading countries", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateCountrySpinner() {
        countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryNames);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countrySpinner.getSelectedItem().toString();
                Log.d("COUNTRY_SELECTED", "Selected: " + selectedCountry + " → Code: " + countryCodeMap.get(selectedCountry));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDatePicker(final Calendar calendar, final EditText targetField) {
        Calendar initialCalendar = Calendar.getInstance();
        int year = initialCalendar.get(Calendar.YEAR);
        int month = initialCalendar.get(Calendar.MONTH);
        int day = initialCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                StepThreeActivity.this,
//                android.R.style.Theme_Material_Light_Dialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    updateLabel(targetField, calendar);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void updateLabel(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

}
