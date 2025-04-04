package com.example.luggageassistant.view.TripConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.view.MainActivity;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import java.util.ArrayList;
import java.util.List;

public class StepTwoActivity extends AppCompatActivity {
    private Button backButton, submitButton;
    private TripConfigurationViewModel tripConfigurationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_two);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        backButton = findViewById(R.id.backButton);
        submitButton = findViewById(R.id.submitButton);

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
        TripConfiguration tripConfiguration = tripConfigurationViewModel.getTripConfiguration();

        // Exemplu de adăugare de noi date colectate în StepTwo
        // tripConfiguration.setCountry("Country");
        // tripConfiguration.setCity("City");
        // tripConfiguration.setTravelPurpose("Vacation");

        TripConfigurationRepository repository = new TripConfigurationRepository();
        repository.saveTripConfiguration(tripConfiguration, new TripConfigurationRepository.OnDataSavedCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(StepTwoActivity.this, "Trip configuration saved successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(StepTwoActivity.this, "Error saving trip configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
