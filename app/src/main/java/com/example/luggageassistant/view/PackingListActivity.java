package com.example.luggageassistant.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.utils.PromptBuilder;
import com.example.luggageassistant.viewmodel.PackingListViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.gson.Gson;

public class PackingListActivity extends AppCompatActivity {

    private PackingListViewModel packingListViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView responseTextView; // asigură-te că ai un TextView în XML cu acest ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        responseTextView = findViewById(R.id.responseTextView);
        responseTextView.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        // Initializezi ViewModel-urile
        packingListViewModel = new ViewModelProvider(this).get(PackingListViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        observePackingList();

        // Preiei configurația curentă
        String tripJson = getIntent().getStringExtra("trip_config");

        if (tripJson == null) {
            responseTextView.setText("Nu s-au găsit datele călătoriei.");
            return;
        }

        TripConfiguration tripConfiguration = new Gson().fromJson(tripJson, TripConfiguration.class);
        String promptJson = PromptBuilder.buildPromptFromTrip(tripConfiguration);

        // Apelezi GPT
        // packingListViewModel.viewPromptforTest(promptJson);
        packingListViewModel.requestPackingList(promptJson);
    }

    private void observePackingList() {
        packingListViewModel.getPackingListLiveData().observe(this, response -> {
            Log.d("GPT_RESPONSE", response);
            responseTextView.setText(response);
        });

        packingListViewModel.getErrorLiveData().observe(this, error -> {
            Toast.makeText(this, "Eroare GPT: " + error, Toast.LENGTH_SHORT).show();
        });
    }
}
