package com.hydrocare.app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int GOAL = 2000;
    private SharedPreferences prefs;
    private final ArrayList<String> history = new ArrayList<>();

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

        Runnable refresh = () -> {
            int intake = prefs.getInt("intake", 0);
            tvIntake.setText(intake + " ml");
            tvGoal.setText("Goal: " + GOAL + " ml");
            progressBar.setProgress(Math.min(intake, GOAL));
            if (intake >= GOAL) {
                tvMotivation.setText("You crushed your goal today!");
            } else if (intake >= GOAL / 2) {
                tvMotivation.setText("Halfway there, keep it up!");
            } else if (intake > 0) {
                tvMotivation.setText("Good start, keep drinking!");
            } else {
                tvMotivation.setText("Start drinking!");
            }
        };

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");

        findViewById(R.id.btn250).setOnClickListener(v -> {
            add(250);
            history.add(LocalTime.now().format(fmt) + "  +250 ml");
            refresh.run();
        });
        findViewById(R.id.btn500).setOnClickListener(v -> {
            add(500);
            history.add(LocalTime.now().format(fmt) + "  +500 ml");
            refresh.run();
        });
        findViewById(R.id.btn750).setOnClickListener(v -> {
            add(750);
            history.add(LocalTime.now().format(fmt) + "  +750 ml");
            refresh.run();
        });
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            prefs.edit().putInt("intake", 0).apply();
            history.clear();
            refresh.run();
        });
        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            String msg = history.isEmpty() ? "No drinks logged yet." : String.join("\n", history);
            new AlertDialog.Builder(this)
                    .setTitle("Today's Log")
                    .setMessage(msg)
                    .setPositiveButton("OK", null)
                    .show();
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
