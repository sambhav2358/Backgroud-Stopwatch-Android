package com.sambhav2358.testing;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    Runnable runnable;
    int seconds = 0;
    String time = "00:00";
    TextView timeTextView;
    boolean isRunning = false;
    public static Intent serviceIntent;
    boolean canRun = true;
    private final String CHANNEL_ID = "Channel_id";
    NotificationManager mNotificationManager;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, TimerService.class);

        timeTextView = findViewById(R.id.textView);
        handler = new Handler();

        findViewById(R.id.start).setOnClickListener(v -> {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,1000);
            isRunning = true;

            findViewById(R.id.lottieAnimationView).setVisibility(View.VISIBLE);

            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,true);
        });

        findViewById(R.id.pause).setOnClickListener(v -> {
            isRunning = false;
            handler.removeCallbacks(runnable);

            findViewById(R.id.lottieAnimationView).setVisibility(View.INVISIBLE);

            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,false);
        });

        findViewById(R.id.reset).setOnClickListener(v -> {
            isRunning = false;
            handler.removeCallbacks(runnable);
            seconds = 0;
            time = "00:00";
            timeTextView.setText(time);

            findViewById(R.id.lottieAnimationView).setVisibility(View.INVISIBLE);

            PrefUtil.setTimerSecondsPassed(this,seconds);
            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,false);
        });

        runnable = () -> {
            if (!canRun) return;

            seconds++;

            Log.d("timerCountActivity", seconds + "");
            long minutes = TimeUnit.SECONDS.toMinutes(seconds);
            String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
            time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));
            timeTextView.setText(time);

            PrefUtil.setTimerSecondsPassed(this, seconds);

            handler.postDelayed(runnable,1000);
        };

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Toast.makeText(this, "no allowed", Toast.LENGTH_SHORT).show();
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(CHANNEL_ID);

            Intent notificationIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            String expandedNotificationText = String.format("Background activity is restricted on this app." +
                    "\nPlease allow it so we can post an active notification during work sessions.\n\n" +
                    "To do so, click on the notification to go to\n" +
                    "App management -> search for %s -> Battery Usage -> enable 'Allow background activity')", getString(R.string.app_name));
            notificationBuilder.setContentIntent(pendingIntent)
                    .setContentText("Background activity is restricted on this device.")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(expandedNotificationText))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true);

            createChannel();

            Notification notification = notificationBuilder.build();

            mNotificationManager.notify(10000, notification);
        }
    }

    @Override
    protected void onPause() {
        startTimerService();
        super.onPause();

        handler.removeCallbacks(runnable);

        PrefUtil.setIsRunningInBackground(this,true);

        canRun = false;
    }

    private void startTimerService() {
        if(isRunning) {
            PrefUtil.setTimerSecondsPassed(this,seconds);
            Intent serviceIntent = new Intent(this, TimerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        seconds = (int) PrefUtil.getTimerSecondsPassed(this);

        isRunning = seconds != 0 && PrefUtil.getWasTimerRunning(this);

        findViewById(R.id.lottieAnimationView).setVisibility(isRunning ? View.VISIBLE : View.INVISIBLE);

        canRun = true;

        if (isRunning) handler.postDelayed(runnable,1000);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
        time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));

        if (timeTextView == null) {
            timeTextView = findViewById(R.id.textView);
        }
        timeTextView.setText(time);

        PrefUtil.setIsRunningInBackground(this,false);

        stopService(serviceIntent);
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "STOPWATCH";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

        mChannel.setName("Notifications");

        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }

        return CHANNEL_ID;
    }
}