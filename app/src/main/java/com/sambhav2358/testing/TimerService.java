package com.sambhav2358.testing;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    long seconds = 0L;
    boolean notificationJustStarted = true;
    public static Handler timerHandler = new Handler();
    public static Runnable timerRunnable;
    private final String CHANNEL_ID = "Channel_id";
    NotificationManager mNotificationManager;
    int prevSeconds;

    NotificationCompat.Builder timerNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(CHANNEL_ID);

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate() {
        super.onCreate();
        String TAG = "Timer Service";
        Log.d(TAG, "onCreate: started service");
        startForeground(1, new NotificationCompat.Builder(TimerService.this, createChannel()).setContentTitle("Goal In Progress").setPriority(NotificationManager.IMPORTANCE_MAX).build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        PrefUtil.setIsRunningInBackground(this, true);

        String goalName = "Sample Goal";
        notificationJustStarted = true;

        prevSeconds = (int) PrefUtil.getTimerSecondsPassed(TimerService.this);

        seconds = prevSeconds;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                updateNotification(goalName, seconds);
                Log.d("timerCount", seconds + "");

                PrefUtil.setTimerSecondsPassed(TimerService.this,seconds);

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);

        return START_STICKY;
    }

    @SuppressLint("NewApi")
    public void updateNotification(String goalName, long seconds) {
        if (notificationJustStarted) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            @SuppressLint("InlinedApi") PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            timerNotificationBuilder.setContentTitle("Goal In Progress")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true);
            notificationJustStarted = false;
        }

        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
        String time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));

        timerNotificationBuilder.setContentText(goalName + " is in progress\nthis session's length: " + time);

        Intent pauseIntent = new Intent(this, StopwatchNotificationActionReceiver.class).putExtra("action", "p");
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
        timerNotificationBuilder.addAction(0, "PAUSE", pendingIntentPause);

        Intent startIntent = new Intent(this, StopwatchNotificationActionReceiver.class).putExtra("action", "s");
        PendingIntent pendingIntentStart = PendingIntent.getBroadcast(this, 10, startIntent, PendingIntent.FLAG_MUTABLE);
        timerNotificationBuilder.addAction(0, "START", pendingIntentStart);

        Intent resetIntent = new Intent(this, StopwatchNotificationActionReceiver.class).putExtra("action", "r");
        PendingIntent pendingIntentReset = PendingIntent.getBroadcast(this, 100, resetIntent, PendingIntent.FLAG_MUTABLE);
        timerNotificationBuilder.addAction(0, "RESET", pendingIntentReset);

        mNotificationManager.notify(1, timerNotificationBuilder.build());

        startForeground(1, timerNotificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        PrefUtil.setTimerSecondsPassed(this, seconds);
        PrefUtil.setIsRunningInBackground(this, false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        } else {
            stopSelf();
        }

        return CHANNEL_ID;
    }
}
