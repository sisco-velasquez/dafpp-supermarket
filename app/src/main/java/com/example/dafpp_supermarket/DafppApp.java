package com.example.dafpp_supermarket;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;

//allows it to "spy" on every single screen in the app
public class DafppApp extends Application implements Application.ActivityLifecycleCallbacks {

    // UPDATE: 1 Hour in milliseconds (1 * 60 * 60 * 1000)
    private static final long TIMEOUT_LIMIT = 1 * 60 * 60 * 1000;

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground (User opened it)
            checkTimeout(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background (User closed/minimized it)
            saveExitTime();
        }
    }
//saves the exact millisecond the user left
    private void saveExitTime() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_exit_time", System.currentTimeMillis());
        editor.apply();
    }
// when user comes back this method runs
    private void checkTimeout(Activity activity) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        long lastExitTime = prefs.getLong("last_exit_time", 0);

        // If it's a fresh install or first run, ignore
        if (lastExitTime == 0) return;

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastExitTime;

        if (timeDifference > TIMEOUT_LIMIT) {
            // TIMEOUT REACHED!

            // 1. Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Redirect to LOGIN Page
            // (Don't redirect if we are already on Login or Register, to avoid loops)
            if (!(activity instanceof Login) && !(activity instanceof Register)) {
                Intent intent = new Intent(activity, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }
        }
    }

    // Required unused methods
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}