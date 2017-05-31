package io.github.micjabbour.androidguard.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mike on 30/05/2017.
 */

public class FcmTokenRequest {
    @SerializedName("fcm_token")
    private String fcmToken;

    public FcmTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}
