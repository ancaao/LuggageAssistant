package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.luggageassistant.R;
import com.example.luggageassistant.view.TripConfiguration.StepOneActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
                Intent intent = new Intent(this, StepOneActivity.class);
                startActivity(intent);
                return false;
            } else if (id == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });

        if (savedInstanceState == null) {
            Fragment defaultFragment;

            if ("final_list".equals(target)) {
                bottomNav.setSelectedItemId(R.id.nav_list);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TripCardListFragment())
                        .commit();

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