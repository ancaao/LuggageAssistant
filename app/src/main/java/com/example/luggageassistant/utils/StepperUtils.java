package com.example.luggageassistant.utils;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.luggageassistant.R;

public class StepperUtils {

    public static void configureStep(Activity activity, int stepNumber) {
        ProgressBar progressCircle = activity.findViewById(R.id.progress_circle);
        TextView stepTitle = activity.findViewById(R.id.step_title);
        TextView progressText = activity.findViewById(R.id.progress_circle_text);

        int progress = 0;
        String title = "";

        switch (stepNumber) {
            case 1:
                progress = 25;
                title = "Personal Information";
                progressText.setText("1 of 4");
                break;
            case 2:
                progress = 50;
                title = "Luggage Details";
                progressText.setText("2 of 4");
                break;
            case 3:
                progress = 75;
                title = "Dates & Location";
                progressText.setText("3 of 4");
                break;
            case 4:
                progress = 100;
                title = "Trip Type";
                progressText.setText("4 of 4");
                break;
        }

        progressCircle.setProgress(progress);
        stepTitle.setText(title);
    }
}