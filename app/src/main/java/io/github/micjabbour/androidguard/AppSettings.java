package io.github.micjabbour.androidguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Mike on 26/05/2017.
 */

public class AppSettings {
    SharedPreferences settings;
    public AppSettings(Context c) {
        settings = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
    }

    public String getCredsUsername() {
        return settings.getString("CredsUsername", "");
    }
    public void setCredsUsername(String username) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("CredsUsername", username);
        editor.apply();
    }

    public String getCredsPassword() {
        return settings.getString("CredsPassword", "");
    }
    public void setCredsPassword(String password) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("CredsPassword", password);
        editor.apply();
    }

    public String getDeviceAuthToken() {
        return settings.getString("DeviceAuthToken", "");
    }
    public void setDeviceAuthToken(String token) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("DeviceAuthToken", token);
        editor.apply();
    }

    public String getDeviceName() { return settings.getString("DeviceName", ""); }
    public void setDeviceName(String deviceName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("DeviceName", deviceName);
        editor.apply();
    }

    public void clearAllAuthInfo() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("CredsUsername", "");
        editor.putString("CredsPassword", "");
        editor.putString("DeviceAuthToken", "");
        editor.putString("DeviceName", "");
        editor.apply();
    }

    public String getSecretNumber() { return settings.getString("SecretNumber", "*#123123#"); }
    public void setSecretNumber(String secretNumber) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SecretNumber", secretNumber);
        editor.apply();
    }
}
