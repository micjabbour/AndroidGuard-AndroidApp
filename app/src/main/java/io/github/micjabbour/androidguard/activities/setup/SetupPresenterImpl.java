package io.github.micjabbour.androidguard.activities.setup;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import io.github.micjabbour.androidguard.AndroidGuardApp;
import io.github.micjabbour.androidguard.AppSettings;
import io.github.micjabbour.androidguard.NetworkService;
import io.github.micjabbour.androidguard.models.DeviceRegRequest;
import io.github.micjabbour.androidguard.models.DeviceRegResponse;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import retrofit2.Response;

/**
 * Created by Mike on 30/05/2017.
 */

public class SetupPresenterImpl implements SetupPresenter {
    SetupView view;
    Disposable disposable;
    AndroidGuardApp application;
    NetworkService service;
    public SetupPresenterImpl(SetupView view, AndroidGuardApp application){
        this.view = view;
        this.application = application;
        this.service = application.getNetworkService();
    }

    @Override
    public boolean hasBackgroundRequests() { return disposable!=null&&!disposable.isDisposed(); }

    @Override
    public void rxUnSubscribe() {
        if(hasBackgroundRequests())
            disposable.dispose();
    }


    @Override
    public void attemptDeviceRegistration(String username, String password, String deviceName) {
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        if(fcmToken == null) {
            fcmToken = "";
        }
        Log.d("SetupPresenter", "fcmToken: "+fcmToken);
        DeviceRegRequest body = new DeviceRegRequest(deviceName, FirebaseInstanceId.getInstance().getToken());
        //TODO: use service.prepareObservable() instead of subscribeOn, observeOn
        Observable<Response<DeviceRegResponse>> responseObservable =
                service.getAPI()
                .registerDevice(Credentials.basic(username,password), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        responseObservable.subscribe(new Observer<Response<DeviceRegResponse>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) { disposable= d; }

            @Override
            public void onNext(@NonNull Response<DeviceRegResponse> deviceRegResponseResponse) {
                if(deviceRegResponseResponse.code() == 401) {
                    view.onRegistrationCredsError();
                    return;
                }
                if(!deviceRegResponseResponse.isSuccessful()) {
                    Log.d("SetupPresenter", "response not successful"+ deviceRegResponseResponse.code());
                    view.onRegistrationNetworkError();
                    return;
                }
                AppSettings settings = new AppSettings(application);
                settings.setDeviceAuthToken(deviceRegResponseResponse.body().getAuthToken());
                if(deviceRegResponseResponse.body().isAlreadyExists())
                    view.onRegistrationDeviceNameAlreadyExists();
                else
                    view.onRegistrationSuccess();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                disposable = null;
                Log.d("SetupPresenter", e.getMessage());
                view.onRegistrationNetworkError();
            }

            @Override
            public void onComplete() {
                disposable = null;
            }
        });

    }

    @Override
    public boolean isValidUsername(String username) {
        //TODO: better username validation logic
        return !TextUtils.isEmpty(username);
    }

    @Override
    public boolean isValidPassword(String password) {
        //TODO: better password validation logic
        return !TextUtils.isEmpty(password);
    }

    @Override
    public boolean isValidDeviceName(String deviceName) {
        //TODO: better deviceName validation logic
        return !TextUtils.isEmpty(deviceName);
    }
}
