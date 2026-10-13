package com.hydrocare.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int GOAL_COOL = 2000;
    private static final int GOAL_WARM = 2500;
    private static final int GOAL_HOT  = 3000;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int NOTIF_PERMISSION_REQUEST    = 1002;

    private int dailyGoal = GOAL_COOL;
    private SharedPreferences prefs;
    private final ArrayList<String> history = new ArrayList<>();
    private boolean pendingReminderEnable = false;

    private TextView       tvIntake;
    private TextView       tvGoalLabel;
    private TextView       tvStreak;
    private TextView       tvMotivation;
    private TextView       tvWeather;
    private ProgressBar    progressBar;
    private MaterialButton btnReminder;
    private HydroChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("hydrocare", MODE_PRIVATE);
        resetIfNewDay();
        loadHistory();

        tvIntake    = findViewById(R.id.tvIntake);
        tvGoalLabel = findViewById(R.id.tvGoalLabel);
        tvStreak    = findViewById(R.id.tvStreak);
        tvMotivation = findViewById(R.id.tvMotivation);
        tvWeather   = findViewById(R.id.tvWeather);
        progressBar = findViewById(R.id.progressBar);
        btnReminder = findViewById(R.id.btnReminder);
        chartView   = findViewById(R.id.chartView);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");

        findViewById(R.id.btn250).setOnClickListener(v -> {
            add(250);
            history.add(LocalTime.now().format(fmt) + "  +250 ml");
            saveHistory();
            refresh();
        });
        findViewById(R.id.btn500).setOnClickListener(v -> {
            add(500);
            history.add(LocalTime.now().format(fmt) + "  +500 ml");
            saveHistory();
            refresh();
        });
        findViewById(R.id.btn750).setOnClickListener(v -> {
            add(750);
            history.add(LocalTime.now().format(fmt) + "  +750 ml");
            saveHistory();
            refresh();
        });
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            prefs.edit()
                 .putInt("intake", 0)
                 .putInt("intake_" + LocalDate.now().toString(), 0)
                 .remove("log")
                 .apply();
            history.clear();
            refresh();
        });
        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            String msg = history.isEmpty() ? "No drinks logged yet." : String.join("\n", history);
            new AlertDialog.Builder(this)
                    .setTitle("Today's Log")
                    .setMessage(msg)
                    .setPositiveButton("OK", null)
                    .show();
        });
        btnReminder.setOnClickListener(v -> toggleReminders());

        ReminderReceiver.createChannel(this);
        updateReminderButton(prefs.getBoolean("reminders_on", false));
        refresh();
        fetchWeatherGoal();
    }

    // ── Reminders ────────────────────────────────────────────────────────────

    private void toggleReminders() {
        if (prefs.getBoolean("reminders_on", false)) {
            cancelReminders(this);
            prefs.edit().putBoolean("reminders_on", false).apply();
            updateReminderButton(false);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    pendingReminderEnable = true;
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIF_PERMISSION_REQUEST);
                    return;
                }
            }
            showIntervalDialog();
        }
    }

    private void showIntervalDialog() {
        String[] options   = {"Every 30 minutes", "Every 1 hour", "Every 2 hours"};
        long[]   intervals = {30 * 60 * 1000L, AlarmManager.INTERVAL_HOUR, 2 * AlarmManager.INTERVAL_HOUR};
        new AlertDialog.Builder(this)
                .setTitle("Remind me...")
                .setItems(options, (d, which) -> {
                    scheduleReminders(this, intervals[which]);
                    prefs.edit()
                         .putBoolean("reminders_on", true)
                         .putLong("reminder_interval", intervals[which])
                         .apply();
                    updateReminderButton(true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateReminderButton(boolean on) {
        btnReminder.setText(on ? "Reminders: ON" : "Reminders: OFF");
    }

    static void scheduleReminders(Context ctx, long intervalMs) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMs, intervalMs, pi);
    }

    static void cancelReminders(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    // ── Streak + refresh ─────────────────────────────────────────────────────

    private void refresh() {
        int intake = prefs.getInt("intake", 0);
        tvIntake.setText(intake + " ml");
        tvGoalLabel.setText("Goal: " + dailyGoal + " ml");
        progressBar.setMax(dailyGoal);
        progressBar.setProgress(Math.min(intake, dailyGoal));

        int streak = prefs.getInt("streak", 0);
        tvStreak.setText(streak > 0 ? streak + "-day streak" : "");

        if (intake >= dailyGoal) {
            tvMotivation.setText("You crushed your goal today!");
        } else if (intake >= dailyGoal / 2) {
            tvMotivation.setText("Halfway there, keep it up!");
        } else if (intake > 0) {
            tvMotivation.setText("Good start, keep drinking!");
        } else {
            tvMotivation.setText("Start drinking!");
        }

        prefs.edit().putInt("goal_" + LocalDate.now().toString(), dailyGoal).apply();
        chartView.loadData(prefs);
    }

    // ── History persistence ───────────────────────────────────────────────────

    private void loadHistory() {
        String saved = prefs.getString("log", "");
        if (!saved.isEmpty()) {
            for (String entry : saved.split("\n")) {
                if (!entry.isEmpty()) history.add(entry);
            }
        }
    }

    private void saveHistory() {
        prefs.edit().putString("log", String.join("\n", history)).apply();
    }

    // ── Intake + day reset ────────────────────────────────────────────────────

    private void add(int ml) {
        int newVal = prefs.getInt("intake", 0) + ml;
        String today = LocalDate.now().toString();
        prefs.edit().putInt("intake", newVal).putInt("intake_" + today, newVal).apply();
    }

    private void resetIfNewDay() {
        String today    = LocalDate.now().toString();
        String savedDay = prefs.getString("day", "");
        if (today.equals(savedDay)) return;

        SharedPreferences.Editor editor = prefs.edit();
        if (!savedDay.isEmpty()) {
            int prevIntake = prefs.getInt("intake", 0);
            int prevGoal   = prefs.getInt("goal_" + savedDay, GOAL_COOL);
            int streak     = prefs.getInt("streak", 0);
            editor.putInt("intake_" + savedDay, prevIntake)
                  .putInt("streak", prevIntake >= prevGoal ? streak + 1 : 0);
        }
        editor.putInt("intake", 0).putString("day", today).remove("log").apply();
    }

    // ── Weather ───────────────────────────────────────────────────────────────

    private void fetchWeatherGoal() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location loc = null;
        try {
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException ignored) {}
        if (loc == null) return;

        double lat = loc.getLatitude();
        double lon = loc.getLongitude();

        new Thread(() -> {
            try {
                URL url = new URL(
                    "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon + "&current=temperature_2m");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                double temp = json.getJSONObject("current").getDouble("temperature_2m");

                int newGoal;
                String weatherMsg;
                if (temp > 32) {
                    newGoal    = GOAL_HOT;
                    weatherMsg = (int) temp + "°C outside — goal bumped to 3000 ml";
                } else if (temp >= 25) {
                    newGoal    = GOAL_WARM;
                    weatherMsg = (int) temp + "°C outside — goal bumped to 2500 ml";
                } else {
                    newGoal    = GOAL_COOL;
                    weatherMsg = (int) temp + "°C outside — goal: 2000 ml";
                }

                runOnUiThread(() -> {
                    dailyGoal = newGoal;
                    tvWeather.setText(weatherMsg);
                    refresh();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (requestCode == LOCATION_PERMISSION_REQUEST && granted) {
            fetchWeatherGoal();
        }
        if (requestCode == NOTIF_PERMISSION_REQUEST && granted && pendingReminderEnable) {
            pendingReminderEnable = false;
            showIntervalDialog();
        }
    }
}
