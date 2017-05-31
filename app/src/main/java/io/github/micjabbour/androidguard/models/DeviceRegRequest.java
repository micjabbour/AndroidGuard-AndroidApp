package io.github.micjabbour.androidguard.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mike on 30/05/2017.
 */

public class DeviceRegRequest {
    @SerializedName("device_name")
    private String deviceName;
    @SerializedName("fcm_token")
    private String fcmToken;

    public DeviceRegRequest(String deviceName, String fcmToken) {
        this.deviceName= deviceName;
        this.fcmToken= fcmToken;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}
