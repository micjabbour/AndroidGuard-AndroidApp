package io.github.micjabbour.androidguard.services.location;

import android.content.Context;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import io.github.micjabbour.androidguard.AndroidGuardApp;

/**
 * Created by Mike on 03/06/2017.
 */

public class LocationUpdateServiceUtils {
    public static final String LOG_TAG = "LocUpdateUtils";
    public enum UpdateMode { NORMAL, //every 1 hour
                            HEAVY //every 5 minutes
                            }
    public static void scheduleLocationUpdateService(Context context, UpdateMode updateMode) {
        // see https://github.com/firebase/firebase-jobdispatcher-android

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //schedule again using new parameters
        int windowStart, windowEnd;
        if(updateMode == UpdateMode.NORMAL) {
            windowStart = 45*60;
            windowEnd = 75*60;
        } else { // HEAVY update mode
            windowStart = 3*60;
            windowEnd = 4*60;
        }
        Job job = dispatcher.newJobBuilder()
                .setService(LocationUpdateJobService.class) // the JobService that will be called
                .setTag(AndroidGuardApp.locationUpdateJobTag)        // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(windowStart, windowEnd))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.mustSchedule(job);
        //perform one location update as soon as poosible, so that we don't have to wait about one
        //hour until we get the first location
        Job oneShotJob = dispatcher.newJobBuilder()
                .setService(LocationUpdateJobService.class)
                .setTag(AndroidGuardApp.locationUpdateOneShotJobTag)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setTrigger(Trigger.executionWindow(0, 60)) //within next miinute
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.mustSchedule(oneShotJob);
    }
}
