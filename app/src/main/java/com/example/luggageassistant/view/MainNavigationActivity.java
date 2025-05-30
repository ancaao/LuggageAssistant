package com.example.luggageassistant.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.luggageassistant.R;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificăm dacă e prima lansare
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            // Lansăm activitatea cu animația și ieșim
            startActivity(new Intent(this, IntroAnimationActivity.class));
            prefs.edit().putBoolean("isFirstLaunch", false).apply();
            finish(); // Închide activitatea curentă, ca să nu se vadă în fundal
            return;
        }

        setContentView(R.layout.activity_main_navigation);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        String target = getIntent().getStringExtra("navigate_to");
        String tripId = getIntent().getStringExtra("trip_id");

        if (tripId != null) {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("current_trip_id", tripId)
                    .apply();
            Log.d("MainNavigation", "Saved tripId: " + tripId);
        }


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            }else if (id == R.id.nav_list) {
                selectedFragment = new TripCardListFragment();
            } else if (id == R.id.nav_add) {
                // Deschide o activitate nouă în loc de fragment
                Intent intent = new Intent(this, StepOneActivity.class);
                startActivity(intent);
                return false; // Nu selecta "+" ca tab activ
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });

        // Fragment implicit la lansare
        if (savedInstanceState == null) {
            Fragment defaultFragment;

            if ("final_list".equals(target)) {
                bottomNav.setSelectedItemId(R.id.nav_list);

                // Mai întâi încarcă TripCardListFragment fără să-l adaugi în back stack
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TripCardListFragment())
                        .commit();

                // Apoi încarcă FinalPackingListFragment și îl adaugi în back stack
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FinalPackingListFragment())
                        .addToBackStack(null)
                        .commit();

                return;
            } else if ("list".equals(target)) {
                defaultFragment = new TripCardListFragment();
                bottomNav.setSelectedItemId(R.id.nav_list);
            } else {
                defaultFragment = new HomeFragment();
                bottomNav.setSelectedItemId(R.id.nav_home);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, defaultFragment)
                    .commit();

        }
    }
}