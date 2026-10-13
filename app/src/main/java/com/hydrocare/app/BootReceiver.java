package com.hydrocare.app;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences("hydrocare", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("reminders_on", false)) return;
        long interval = prefs.getLong("reminder_interval", AlarmManager.INTERVAL_HOUR);
        MainActivity.scheduleReminders(context, interval);
    }
}
