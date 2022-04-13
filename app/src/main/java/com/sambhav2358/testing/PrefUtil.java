package com.sambhav2358.testing;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {
    public static long getTimerSecondsPassed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        return preferences.getLong("seconds",0);
    }

    public static void setTimerSecondsPassed(Context context, long seconds) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        preferences.edit().putLong("seconds", seconds).apply();
    }

    public static boolean getIsRunningInBackground(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        return preferences.getBoolean("iR",false);
    }

    public static void setIsRunningInBackground(Context context, boolean isRunnign) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        preferences.edit().putBoolean("iR", isRunnign).apply();
    }

    public static boolean getWasTimerRunning(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        return preferences.getBoolean("iTR",false);
    }

    public static void setWasTimerRunning(Context context, boolean isRunnign) {
        SharedPreferences preferences = context.getSharedPreferences("abc",Context.MODE_PRIVATE);
        preferences.edit().putBoolean("iTR", isRunnign).apply();
    }
}
