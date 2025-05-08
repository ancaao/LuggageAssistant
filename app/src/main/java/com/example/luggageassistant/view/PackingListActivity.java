package com.example.luggageassistant.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PackingListEntry;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.utils.PackingListParser;
import com.example.luggageassistant.utils.PromptBuilder;
import com.example.luggageassistant.view.adapter.PackingGroupedAdapter;
import com.example.luggageassistant.view.adapter.PackingItemAdapter;
import com.example.luggageassistant.viewmodel.PackingListViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.gson.Gson;

import java.util.List;

public class PackingListActivity extends AppCompatActivity {

    private PackingListViewModel packingListViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private TextView statusText;
    private RecyclerView recyclerView;
//    private PackingItemAdapter adapter;
    private PackingGroupedAdapter adapter;
    private ProgressBar loadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        // Inițializează componentele UI
        statusText = findViewById(R.id.statusText);
        statusText.setVisibility(View.GONE); // ascuns inițial

        recyclerView = findViewById(R.id.packingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        loadingIndicator = findViewById(R.id.loadingIndicator);


        // ViewModel-uri
        packingListViewModel = new ViewModelProvider(this).get(PackingListViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        // Observator pentru răspunsul GPT
        packingListViewModel.getPackingListLiveData().observe(this, response -> {
            try {
                Log.d("GPT_RAW_JSON", response);
                List<PackingListEntry> grouped = PackingListParser.parseGrouped(response);

                loadingIndicator.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);

                adapter = new PackingGroupedAdapter(grouped);
                recyclerView.setAdapter(adapter);
            } catch (Exception e) {
                statusText.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
                statusText.setText("Error processing the list of items.");
                Log.e("ParseError", e.getMessage(), e);
            }
        });

        // Observator pentru erori GPT
        packingListViewModel.getErrorLiveData().observe(this, error -> {
            loadingIndicator.setVisibility(View.GONE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("GPT generated error:\n" + error);
            Toast.makeText(this, "GPT error: " + error, Toast.LENGTH_SHORT).show();
        });

        // Preluăm datele de configurare
        String tripJson = getIntent().getStringExtra("trip_config");
        if (tripJson == null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("No trip configuration data found.");
            return;
        }

        TripConfiguration tripConfiguration = new Gson().fromJson(tripJson, TripConfiguration.class);
        String promptJson = PromptBuilder.buildPromptFromTrip(tripConfiguration);

        // Trimitem request către GPT
        loadingIndicator.setVisibility(View.VISIBLE);
        packingListViewModel.requestPackingList(promptJson);
    }
}
