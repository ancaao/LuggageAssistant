package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PersonPackingList;
import com.example.luggageassistant.model.TripConfiguration;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.utils.PackingListParser;
import com.example.luggageassistant.utils.PromptBuilder;
import com.example.luggageassistant.utils.Triple;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.example.luggageassistant.view.adapter.PackingPagerAdapter;
import com.example.luggageassistant.viewmodel.PackingListViewModel;
import com.example.luggageassistant.viewmodel.TripConfigurationViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackingListActivity extends AppCompatActivity {

    private PackingListViewModel packingListViewModel;
    private TripConfigurationViewModel tripConfigurationViewModel;
    private LottieAnimationView lottieAnimationView;
    private TextView statusText;
    private LinearLayout loadingLayout;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button saveButton;
    private List<PersonPackingList> personLists;
    private String tripId;
    private Button tryAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        statusText = findViewById(R.id.statusText);
        loadingLayout = findViewById(R.id.loadingLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        saveButton = findViewById(R.id.saveButton);
        tryAgainButton = findViewById(R.id.tryAgainButton);

        loadingLayout.setVisibility(View.GONE);
        lottieAnimationView.cancelAnimation();
        saveButton.setVisibility(View.GONE);

        tabLayout.setVisibility(View.GONE);

        packingListViewModel = new ViewModelProvider(this).get(PackingListViewModel.class);
        tripConfigurationViewModel = new ViewModelProvider(this).get(TripConfigurationViewModel.class);

        tryAgainButton.setOnClickListener(v -> {
            tripConfigurationViewModel.resetTripConfiguration();
            Intent intent = new Intent(PackingListActivity.this, StepOneActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // închide activitatea curentă
        });

        // 🔽 Primim datele din intent
        String tripJson = getIntent().getStringExtra("trip_config");
        tripId = getIntent().getStringExtra("trip_id");

        if (tripJson == null || tripId == null) {
            loadingLayout.setVisibility(View.VISIBLE);
            lottieAnimationView.cancelAnimation();
            statusText.setText("Missing trip configuration or ID.");
            return;
        }

        TripConfiguration tripConfiguration = new Gson().fromJson(tripJson, TripConfiguration.class);
        String promptJson = PromptBuilder.buildPromptFromTrip(tripConfiguration);

        // 🔽 Trimitem promptul către GPT
        loadingLayout.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();
        statusText.setText("Working on you packing list suggestion...");

        packingListViewModel.requestPackingList(promptJson, tripId);

        // 🔽 Observăm rezultatul
        packingListViewModel.getPackingListLiveData().observe(this, response -> {
            try {
                Log.d("GPT_JSON_RESPONSE", response);
                this.personLists = PackingListParser.parsePerPerson(response);

                loadingLayout.setVisibility(View.GONE);
                lottieAnimationView.cancelAnimation();

                tryAgainButton.setVisibility(View.GONE);
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
                tabLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                loadingLayout.setVisibility(View.VISIBLE);
                lottieAnimationView.cancelAnimation();
                statusText.setText("Error processing the list.");
                tryAgainButton.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                Log.e("PackingListParse", "Error parsing JSON", e);
            }
        });

        packingListViewModel.getErrorLiveData().observe(this, error -> {
            loadingLayout.setVisibility(View.VISIBLE);
            lottieAnimationView.cancelAnimation();
            statusText.setText("GPT Error: " + error);
            tryAgainButton.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
        });

        // 🔽 Salvăm doar la apăsarea pe buton

        saveButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            List<Triple<String, String, PackingItem>> itemsToSave = new ArrayList<>(); // personName, category, item

            for (PersonPackingList person : personLists) {
                String personName = person.getPersonName();

                for (Map.Entry<String, List<PackingItem>> entry : person.getCategorizedItems().entrySet()) {
                    Log.d("SAVE_PROCESS", "Verific persoana: " + personName);

                    for (PackingItem item : entry.getValue()) {
                        Log.d("SAVE_PROCESS", "  ↳ Item: " + item.getItem() + " | checked = " + item.isChecked());
                        if (item.isChecked()) {
                            itemsToSave.add(new Triple<>(userId, personName, item));
                        }
                    }
                }
            }

            if (itemsToSave.isEmpty()) {
                Toast.makeText(this, "No items selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            final int totalItems = itemsToSave.size();
            final int[] savedCount = {0};

            Log.d("SAVE_PROCESS", "TOTAL items to save: " + itemsToSave.size());
            for (Triple<String, String, PackingItem> triple : itemsToSave) {
                Log.d("SAVE_PROCESS", "→ Ready to save: " + triple.getThird().getItem() + " for " + triple.getSecond());
                String personName = triple.getSecond();
                PackingItem item = triple.getThird();

                item.setPersonName(personName);

                PackingListRepository.getInstance()
                        .saveFinalPackingItem(userId, tripId, personName, item, () -> {
                    savedCount[0]++;
                    if (savedCount[0] == totalItems) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Packing list saved!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PackingListActivity.this, MainNavigationActivity.class);
                            intent.putExtra("navigate_to", "final_list");
                            intent.putExtra("trip_id", tripId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }
        });

    }
}
