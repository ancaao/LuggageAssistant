package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PersonPackingList;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.utils.PackingListParser;
import com.example.luggageassistant.utils.PromptBuilder;
import com.example.luggageassistant.view.adapter.PackingPagerAdapter;
import com.example.luggageassistant.viewmodel.PackingListViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class PackingListActivity extends AppCompatActivity {

    private PackingListViewModel packingListViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private ProgressBar loadingIndicator;
    private TextView statusText;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button saveButton;
    private List<PersonPackingList> personLists;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        loadingIndicator = findViewById(R.id.loadingIndicator);
        statusText = findViewById(R.id.statusText);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        saveButton = findViewById(R.id.saveButton);

        statusText.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);

        packingListViewModel = new ViewModelProvider(this).get(PackingListViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        // 🔽 Primim datele din intent
        String tripJson = getIntent().getStringExtra("trip_config");
        tripId = getIntent().getStringExtra("trip_id");

        if (tripJson == null || tripId == null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("Missing trip configuration or ID.");
            return;
        }

        TripConfiguration tripConfiguration = new Gson().fromJson(tripJson, TripConfiguration.class);
        String promptJson = PromptBuilder.buildPromptFromTrip(tripConfiguration);

        // 🔽 Trimitem promptul către GPT
        loadingIndicator.setVisibility(View.VISIBLE);
        statusText.setText("Generating packing list...");
        statusText.setVisibility(View.VISIBLE);
        packingListViewModel.requestPackingList(promptJson, tripId);

        // 🔽 Observăm rezultatul
        packingListViewModel.getPackingListLiveData().observe(this, response -> {
            try {
                Log.d("GPT_JSON_RESPONSE", response);
                this.personLists = PackingListParser.parsePerPerson(response);

                loadingIndicator.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);

                PackingPagerAdapter adapter = new PackingPagerAdapter(this, personLists);
                viewPager.setAdapter(adapter);

                viewPager.setClipToPadding(false);
                viewPager.setPadding(80, 0, 80, 0);
                viewPager.setOffscreenPageLimit(1);
                viewPager.setPageTransformer((page, position) -> {
                    float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
                    page.setScaleY(scale);
                    page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
                    page.setTranslationX(-30 * position);
                });

                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    tab.setText(personLists.get(position).getPersonName());
                }).attach();

                tabLayout.setOnTouchListener((v, event) -> true);

            } catch (Exception e) {
                loadingIndicator.setVisibility(View.GONE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText("Error processing the list.");
                Log.e("PackingListParse", "Error parsing JSON", e);
            }
        });

        packingListViewModel.getErrorLiveData().observe(this, error -> {
            loadingIndicator.setVisibility(View.GONE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("GPT Error: " + error);
            Toast.makeText(this, "GPT Error: " + error, Toast.LENGTH_SHORT).show();
        });

        // 🔽 Salvăm doar la apăsarea pe buton
        saveButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            for (PersonPackingList person : personLists) {
                String personName = person.getPersonName();
                for (Map.Entry<String, List<PackingItem>> entry : person.getCategorizedItems().entrySet()) {
                    for (PackingItem item : entry.getValue()) {
                        if (item.isChecked()) {
                            PackingListRepository.getInstance().savePackingItem(userId, tripId, personName, item);
                        }
                    }
                }
            }

            Toast.makeText(this, "Packing list saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PackingListActivity.this, MainNavigationActivity.class);
            intent.putExtra("navigate_to", "home");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
