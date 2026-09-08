package com.hydrocare.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    private static final int GOAL = 2000;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("hydrocare", MODE_PRIVATE);
        resetIfNewDay();

        TextView tvIntake = findViewById(R.id.tvIntake);
        TextView tvGoal = findViewById(R.id.tvGoalLabel);
        TextView tvMotivation = findViewById(R.id.tvMotivation);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        MaterialButton btn250 = findViewById(R.id.btn250);
        MaterialButton btn500 = findViewById(R.id.btn500);
        MaterialButton btn750 = findViewById(R.id.btn750);
        MaterialButton btnReset = findViewById(R.id.btnReset);

        Runnable refresh = () -> {
            int intake = prefs.getInt("intake", 0);
            tvIntake.setText(String.valueOf(intake));
            tvGoal.setText(intake + " / " + GOAL + " ml");
            progressBar.setProgress(Math.min(intake, GOAL));
            if (intake >= GOAL) {
                tvMotivation.setText("You crushed your goal today!");
            } else if (intake >= GOAL / 2) {
                tvMotivation.setText("Halfway there, keep it up!");
            } else if (intake > 0) {
                tvMotivation.setText("Good start, keep drinking!");
            } else {
                tvMotivation.setText("Tap a button below to log your first drink!");
            }
        };

        btn250.setOnClickListener(v -> { add(250); refresh.run(); });
        btn500.setOnClickListener(v -> { add(500); refresh.run(); });
        btn750.setOnClickListener(v -> { add(750); refresh.run(); });
        btnReset.setOnClickListener(v -> {
            prefs.edit().putInt("intake", 0).apply();
            refresh.run();
        });

        refresh.run();
    }

    private void add(int ml) {
        int current = prefs.getInt("intake", 0);
        prefs.edit().putInt("intake", current + ml).apply();
    }

    private void resetIfNewDay() {
        String today = LocalDate.now().toString();
        if (!today.equals(prefs.getString("day", ""))) {
            prefs.edit().putInt("intake", 0).putString("day", today).apply();
        }
    }
}
