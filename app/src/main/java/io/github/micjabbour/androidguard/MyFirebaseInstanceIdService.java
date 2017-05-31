package io.github.micjabbour.androidguard;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.github.micjabbour.androidguard.models.FcmTokenRequest;
import io.github.micjabbour.androidguard.models.FcmTokenResponse;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    public static final String TAG = "FCMInstanceIdService";
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        // sendRegistrationToServer(refreshedToken);
        AppSettings settings = new AppSettings(this);
        //do not send update request to server if the device is not registered yet
        if(settings.getDeviceAuthToken().equals("")) return;
        FcmTokenRequest body = new FcmTokenRequest(refreshedToken);
        NetworkService service = ((AndroidGuardApp)getApplication()).getNetworkService();
        //TODO: use service.prepareObservable() instead of subscribeOn, observeOn
        Observable<Response<FcmTokenResponse>> responseObservable =
                service.getAPI()
                .updateFcmToken(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        responseObservable.subscribe(new Observer<Response<FcmTokenResponse>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Response<FcmTokenResponse> fcmTokenResponseResponse) {
                if(!fcmTokenResponseResponse.isSuccessful())
                    Log.d(TAG, "updated FCM token on server");
                else
                    Log.e(TAG, "failed to update FCM token");
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
