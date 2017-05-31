package io.github.micjabbour.androidguard.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mike on 30/05/2017.
 */

public class DeviceRegResponse {
    @SerializedName("token")
    private String authToken;
    @SerializedName("already_exists")
    private boolean alreadyExists;

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public String getAuthToken() {
        return authToken;
    }
}
