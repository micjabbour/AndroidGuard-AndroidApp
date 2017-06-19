package io.github.micjabbour.androidguard.services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.io.File;

import io.github.micjabbour.androidguard.AndroidGuardApp;

/**
 * Created by Mike on 05/06/2017.
 */

public class ClearDataJobService extends JobService {
    private static final String LOG_TAG="ClearDataJobService";

    public static void reschedule(Context context) {
        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job job = dispatcher.newJobBuilder()
                .setService(ClearDataJobService.class)
                .setTag(AndroidGuardApp.clearDataJobTag)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(0, 60)) //within next miinute
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .build();
        dispatcher.mustSchedule(job);
    }
    @Override
    public boolean onStartJob(final JobParameters job) {
        Thread wipingThread = new Thread(){
            @Override
            public void run() {
                if(Utils.wipeAllData())
                    Log.d(LOG_TAG, "wiped successfully");
                else
                    Log.d(LOG_TAG, "failed to wipe");
                Log.d(LOG_TAG, "finishing jobService. . .");
                jobFinished(job, false);
            }
        };
        wipingThread.start();

        //wipingThread still need to run
        //(keep this job running until a call is made to jobFinished)
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.d(LOG_TAG, "onStopJob(), this: "+this);
        return true;
    }

    private static class Utils {

        static boolean wipeAllData() {
            try {
                wipeDirectory(Environment.getExternalStorageDirectory().toString());
            } catch (Exception e) {
                Log.d(LOG_TAG, "wipe memory card exception");
                return false;
            }
            return true;
        }
        //see https://stackoverflow.com/a/7434897
        static void wipeDirectory(String name) {
            File directoryFile = new File(name);
            File[] filenames = directoryFile.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                directoryFile.delete();
            }
        }

    }
}
