package io.github.micjabbour.androidguard.activities.status;

/**
 * Created by Mike on 31/05/2017.
 */

public interface StatusView {
    public enum Status {
        CONNECTED,
        DISCONNECTED,
        AUTHENTICATION_ERROR,
        UNKOWN_ERROR
    }

    void setStatus(Status s);
}
