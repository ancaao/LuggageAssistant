package com.example.luggageassistant.view.TripConfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.TripConfigurationRepository;
import com.example.luggageassistant.view.MainActivity;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StepThreeActivity extends AppCompatActivity {
    private Button backButton, nextButton;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private Spinner countrySpinner;
    private final Map<String, String> countryCodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_step_three);

        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        backButton = findViewById(R.id.stepThreeBackButton);
        nextButton = findViewById(R.id.stepThreeNextButton);
        countrySpinner = findViewById(R.id.countrySpinner);

        fetchCountriesFromApi();

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countrySpinner.getSelectedItem().toString();
                String countryCode = convertCountryNameToCode(selectedCountry);
                fetchCitiesFromApi(countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nextButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StepFourActivity.class);
            startActivity(intent);
        });

    }


    private void fetchCountriesFromApi() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://wft-geo-db.p.rapidapi.com/v1/geo/countries?limit=10")
                .get()
                .addHeader("X-RapidAPI-Key", "2037c64fdcmsheee893dbd7b8bfap1b8352jsnc0df55306678")
                .addHeader("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(StepThreeActivity.this, "Failed to load countries", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("StepThreeActivityAAAA", "JSON data: " + response.body().string());
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("StepThreeActivityAAAA", "JSON data: " + jsonData);
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray dataArray = jsonObject.getJSONArray("data");

                        List<String> countryNames = new ArrayList<>();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject country = dataArray.getJSONObject(i);
                            String code = country.getString("code");
                            String name = country.getString("name");

                            countryNames.add(name);
                            countryCodeMap.put(name, code);
                        }

                        runOnUiThread(() -> {
                            Spinner countrySpinner = findViewById(R.id.countrySpinner);

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    StepThreeActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    countryNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            countrySpinner.setAdapter(adapter);

                            // Setăm listener pentru orașe după ce spinnerul e populat
                            countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedCountry = countrySpinner.getSelectedItem().toString();
                                    String countryCode = countryCodeMap.get(selectedCountry);
                                    if (countryCode != null) {
                                        fetchCitiesFromApi(countryCode);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchCitiesFromApi(String countryCode) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://wft-geo-db.p.rapidapi.com/v1/geo/countries/" + countryCode + "/cities?limit=10&sort=name";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", "your_api_key_here")
                .addHeader("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(StepThreeActivity.this, "API error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject obj = new JSONObject(jsonData);
                        JSONArray data = obj.getJSONArray("data");

                        List<String> cities = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject city = data.getJSONObject(i);
                            cities.add(city.getString("name"));
                        }

                        runOnUiThread(() -> {
                            Spinner citySpinner = findViewById(R.id.citySpinner);
                            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                                    StepThreeActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    cities
                            );
                            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            citySpinner.setAdapter(cityAdapter);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String convertCountryNameToCode(String countryName) {
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            if (locale.getDisplayCountry().equalsIgnoreCase(countryName)) {
                return countryCode;
            }
        }
        return "RO"; // fallback
    }

}
