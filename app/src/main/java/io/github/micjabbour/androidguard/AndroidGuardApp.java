package io.github.micjabbour.androidguard;

import android.app.Application;

/**
 * Created by Mike on 30/05/2017.
 */

public class AndroidGuardApp extends Application {
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
