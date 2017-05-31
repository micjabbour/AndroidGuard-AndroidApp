package io.github.micjabbour.androidguard.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mike on 30/05/2017.
 */

public class LocationUpdateRequest {
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;


    public LocationUpdateRequest(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
