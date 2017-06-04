package io.github.micjabbour.androidguard.services;


import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.github.micjabbour.androidguard.AndroidGuardApp;
import io.github.micjabbour.androidguard.NetworkService;
import io.github.micjabbour.androidguard.models.LocationUpdateRequest;
import io.github.micjabbour.androidguard.models.LocationUpdateResponse;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class LocationUpdateJobService extends JobService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private NetworkService mNetworkService;
    private GoogleApiClient mGoogleApiClient;
    private Disposable mDisposable;
    private JobParameters mJob;
    public static final String LOG_TAG = "LocUpdateService";

    //a static function to schedule the JobService
    public static void reschedule(Context context) {
        // see https://github.com/firebase/firebase-jobdispatcher-android

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //schedule again using new parameters
        Job job = dispatcher.newJobBuilder()
                .setService(LocationUpdateJobService.class) // the JobService that will be called
                .setTag(AndroidGuardApp.locationUpdateJobTag)        // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(30*60, 60*60))
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

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkService = ((AndroidGuardApp)getApplication()).getNetworkService();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        mJob = job;
        mGoogleApiClient.connect();

        //callbacks still need to run
        //(keep this job running until a call is made to jobFinished)
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Get last location
        Location lastLocation = null;
        try {
            //TODO: request permissions from user instead of catching the exeptions silently
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "getLastLocation security exception: "+e.getMessage());
        }

        //see https://stackoverflow.com/a/22718415
        //if no location is returned, or location is older than 5 minutes
        if(lastLocation==null || AgeInMinutes(lastLocation)>5) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        locationRequest, this);
            } catch (SecurityException e) {
                Log.e(LOG_TAG, "requestLocationUpdates security exception: "+e.getMessage());
            }
        } else { //it is okay to send lastLocation to webservice
            mGoogleApiClient.disconnect();
            sendToWebService(lastLocation);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        sendToWebService(location);
    }

    //send location to web service, and finishes the job service when done
    private void sendToWebService(Location location) {
        LocationUpdateRequest updateRequest = new LocationUpdateRequest(
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())
        );
        //TODO: use service.prepareObservable() instead of subscribeOn, observeOn
        Observable<Response<LocationUpdateResponse>> responseObservable =
                mNetworkService.getAPI()
                .updateDeviceLocation(updateRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        responseObservable.subscribe(new Observer<Response<LocationUpdateResponse>>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                mDisposable= d;
            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Response<LocationUpdateResponse> locationUpdateResponseResponse) {
                if(locationUpdateResponseResponse.isSuccessful())
                    jobFinished(mJob, false); //does not need reschedule
                else
                    jobFinished(mJob, false); //most likely server-side failure (rescheduling does not help)
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                mDisposable= null;
                jobFinished(mJob, true); //error, needs reschedule
            }

            @Override
            public void onComplete() {
                mDisposable= null;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        if(mDisposable!=null && !mDisposable.isDisposed()) mDisposable.dispose();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "google api client location suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "google api client connection failed");
    }


    public int AgeInMinutes(Location last) {
        return (int) (AgeInMs(last) / (60*1000));
    }

    public long AgeInMs(Location last) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            return ageInMs_Api17(last);
        return ageInMs_PreApi17(last);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long ageInMs_Api17(Location last) {
        return (SystemClock.elapsedRealtimeNanos() - last
                .getElapsedRealtimeNanos()) / 1000000;
    }

    private long ageInMs_PreApi17(Location last) {
        return System.currentTimeMillis() - last.getTime();
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
}
