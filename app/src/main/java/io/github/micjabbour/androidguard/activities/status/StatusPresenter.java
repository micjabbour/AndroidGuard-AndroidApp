package io.github.micjabbour.androidguard.activities.status;

/**
 * Created by Mike on 31/05/2017.
 */

public interface StatusPresenter {
    void checkStatus();
    boolean hasBackgroundRequests();
    void showAppIcon(boolean show);
    boolean isAppHidden();
    void rxUnSubscribe();
}
