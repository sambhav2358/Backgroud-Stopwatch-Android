package com.sambhav2358.testing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    Runnable runnable;
    int seconds = 0;
    String time = "00:00";
    TextView timeTextView;
    boolean isRunning = false;
    Intent serviceIntent;

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

            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,true);
        });

        findViewById(R.id.pause).setOnClickListener(v -> {
            isRunning = false;
            handler.removeCallbacks(runnable);

            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,false);
        });

        findViewById(R.id.reset).setOnClickListener(v -> {
            isRunning = false;
            handler.removeCallbacks(runnable);
            seconds = 0;
            time = "00:00";
            timeTextView.setText(time);

            PrefUtil.setTimerSecondsPassed(this,seconds);
            PrefUtil.setIsRunningInBackground(this,false);
            PrefUtil.setWasTimerRunning(this,false);
        });

        runnable = () -> {
            seconds++;
            long minutes = TimeUnit.SECONDS.toMinutes(seconds);
            String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
            time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));
            timeTextView.setText(time);

            PrefUtil.setTimerSecondsPassed(this, seconds);

            handler.postDelayed(runnable,1000);
        };
    }

    @Override
    protected void onPause() {
        startTimerService();
        super.onPause();

        handler.removeCallbacks(runnable);

        PrefUtil.setIsRunningInBackground(this,true);
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

        if (isRunning) handler.postDelayed(runnable,1000);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        String mins = String.valueOf(minutes).length() == 2 ? minutes + "" : "0" + minutes;
        time = mins + ":" + (String.valueOf(seconds - TimeUnit.MINUTES.toSeconds(minutes)).length() == 2 ? (seconds - TimeUnit.MINUTES.toSeconds(minutes)) : "0" + (seconds - TimeUnit.MINUTES.toSeconds(minutes)));

        if (timeTextView != null){
            timeTextView.setText(time);
        }else {
            timeTextView = findViewById(R.id.textView);
            timeTextView.setText(time);
        }

        PrefUtil.setIsRunningInBackground(this,false);

        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        if (!isRunning) PrefUtil.setTimerSecondsPassed(this,0);
        else Toast.makeText(this, "impossible - " + isRunning, Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}