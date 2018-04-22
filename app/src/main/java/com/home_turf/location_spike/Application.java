package com.home_turf.location_spike;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by valchapple on 4/10/18.
 * https://github.com/aws-samples/aws-mobile-android-notes-tutorial/blob/master/app/src/main/AndroidManifest.xml
 */

// Allows us to create a Singleton for our AWS connection
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the AWS Provider as a Singleton
//        LocationHandler.initialize(getApplicationContext());

        registerActivityLifecycleCallbacks(new ActivityLifeCycle());
    }

    class ActivityLifeCycle implements android.app.Application.ActivityLifecycleCallbacks {
        private int depth = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (depth == 0) {
                Log.d("ActivityLifeCycle", "Application entered foreground");
            }
            depth++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            depth--;
            if (depth == 0) {
                Log.d("ActivityLifeCycle", "Application entered background");
            }

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
