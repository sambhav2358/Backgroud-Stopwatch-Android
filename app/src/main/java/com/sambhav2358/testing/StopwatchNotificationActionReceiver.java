package com.sambhav2358.testing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class StopwatchNotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getStringExtra("action")){
            case "p":
                Log.v("actionClicked", "p");
                
                TimerService.timerHandler.removeCallbacks(TimerService.timerRunnable);
                PrefUtil.setIsRunningInBackground(context, false);
                PrefUtil.setWasTimerRunning(context, false);
                break;
            case "s":
                Log.v("actionClicked", "s");
                
                TimerService.timerHandler.removeCallbacks(TimerService.timerRunnable);
                TimerService.timerHandler.postDelayed(TimerService.timerRunnable, 1000);
                PrefUtil.setIsRunningInBackground(context, true);
                PrefUtil.setWasTimerRunning(context, true);
                break;
            case "r":
                Log.v("actionClicked", "r");
                
                TimerService.timerHandler.removeCallbacks(TimerService.timerRunnable);
                PrefUtil.setIsRunningInBackground(context, false);
                PrefUtil.setTimerSecondsPassed(context, 0);
                PrefUtil.setWasTimerRunning(context, false);
                context.stopService(MainActivity.serviceIntent);
                break;

        }
    }
}
