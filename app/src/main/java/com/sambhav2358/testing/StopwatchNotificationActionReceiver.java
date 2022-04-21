package com.sambhav2358.testing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

                Intent intent1 = new Intent();
                intent1.setClassName("com.sambhav2358.testing", "com.sambhav2358.testing.MainActivity");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent1);
                break;

        }
    }
}
