package io.github.micjabbour.androidguard;

import android.app.Application;

/**
 * Created by Mike on 30/05/2017.
 */

public class AndroidGuardApp extends Application {
    public static final String locationUpdateJobTag = "location-update-job-tag";
    public static final String locationUpdateOneShotJobTag = "location-update-one-shot-job-tag";
    public static final String locationUpdateSMSJobTag = "location-update-sms-job-tag";
    public static final String clearDataJobTag = "clear-data-card-job-tag";
    private NetworkService networkService;
    @Override
    public void onCreate() {
        super.onCreate();
        networkService = new NetworkService(this);
    }

    public String getDeviceAuthToken() {
        AppSettings settings = new AppSettings(this);
        return settings.getDeviceAuthToken();
    }

    public NetworkService getNetworkService() {
        return networkService;
    }
}
