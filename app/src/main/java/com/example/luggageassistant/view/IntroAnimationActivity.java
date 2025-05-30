package com.example.luggageassistant.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.luggageassistant.R;

public class IntroAnimationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_animation);

        LottieAnimationView lottie = findViewById(R.id.intro_lottie);
        lottie.setAnimation("home_loading.lottie"); // sau .lottie dacă ai suport
        lottie.playAnimation();

        // După ce s-a terminat animația (sau ai făcut loading), treci la activitatea principală
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(IntroAnimationActivity.this, MainNavigationActivity.class));
            finish();
        }, 3000); // sau mai mult, dacă ai nevoie
    }
}
