package io.github.micjabbour.androidguard.activities.setup;

/**
 * Created by Mike on 30/05/2017.
 */

public interface SetupPresenter {
    void attemptDeviceRegistration(String username, String password, String deviceName);
    boolean isValidUsername(String username);
    boolean isValidPassword(String password);
    boolean isValidDeviceName(String deviceName);
    boolean hasBackgroundRequests();
    void rxUnSubscribe();
}
