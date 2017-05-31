package io.github.micjabbour.androidguard.activities.setup;

/**
 * Created by Mike on 30/05/2017.
 */

public interface SetupView {
    String getStringFromResId(int resId);
    void onRegistrationSuccess();
    void onRegistrationCredsError();
    void onRegistrationUnkownError();
    void onRegistrationNetworkError();
    void onRegistrationDeviceNameAlreadyExists();
}
