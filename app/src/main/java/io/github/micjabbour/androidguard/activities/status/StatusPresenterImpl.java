package io.github.micjabbour.androidguard.activities.status;

import io.github.micjabbour.androidguard.AndroidGuardApp;
import io.github.micjabbour.androidguard.services.location.LocationUpdateServiceUtils;
import io.github.micjabbour.androidguard.NetworkService;
import io.github.micjabbour.androidguard.ShowHideApp;
import io.github.micjabbour.androidguard.models.TestTokenResponse;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by Mike on 31/05/2017.
 */

public class StatusPresenterImpl implements StatusPresenter {
    StatusView view;
    Disposable disposable;
    AndroidGuardApp application;
    NetworkService service;

    public StatusPresenterImpl(StatusView view, AndroidGuardApp application) {
        this.view = view;
        this.application = application;
        this.service = application.getNetworkService();
    }


    @Override
    public boolean hasBackgroundRequests() { return disposable!=null&&!disposable.isDisposed();}

    @Override
    public void showAppIcon(boolean show) {
        ShowHideApp.showAppIcon(application, show);
    }

    @Override
    public boolean isAppHidden() {
        return ShowHideApp.isAppHidden(application);
    }


    @Override
    public void checkStatus() {
        Observable<Response<TestTokenResponse>> responseObservable =
                service.getAPI()
                .testRequest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        responseObservable.subscribe(new Observer<Response<TestTokenResponse>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) { disposable=d; }

            @Override
            public void onNext(@NonNull Response<TestTokenResponse> testTokenResponseResponse) {
                if(testTokenResponseResponse.code() == 401) {
                    view.setStatus(StatusView.Status.AUTHENTICATION_ERROR);
                } else if(!testTokenResponseResponse.isSuccessful()) {
                    view.setStatus(StatusView.Status.UNKOWN_ERROR);
                } else {
                    view.setStatus(StatusView.Status.CONNECTED);
                    //schedule location update service
                    LocationUpdateServiceUtils.scheduleLocationUpdateService(application, LocationUpdateServiceUtils.UpdateMode.NORMAL);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                view.setStatus(StatusView.Status.DISCONNECTED);
                disposable = null;
            }

            @Override
            public void onComplete() {
                disposable = null;
            }
        });
    }


    @Override
    public void rxUnSubscribe() {
        if(hasBackgroundRequests()) disposable.dispose();
    }
}
