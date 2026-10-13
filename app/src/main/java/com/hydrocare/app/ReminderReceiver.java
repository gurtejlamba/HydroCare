package com.hydrocare.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import java.time.LocalDate;
import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL_ID = "hydrocare_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 7 || hour >= 22) return;

        SharedPreferences prefs = context.getSharedPreferences("hydrocare", Context.MODE_PRIVATE);
        int intake = prefs.getInt("intake", 0);
        int goal   = prefs.getInt("goal_" + LocalDate.now().toString(), 2000);
        if (intake >= goal) return;

        int remaining = goal - intake;

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("Time to hydrate!")
                .setContentText(remaining + " ml left to reach your goal.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, builder.build());
    }

    static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Hydration Reminders", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Periodic reminders to drink water");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
    }
}
