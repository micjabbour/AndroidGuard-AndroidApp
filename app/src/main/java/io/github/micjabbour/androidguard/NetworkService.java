package io.github.micjabbour.androidguard;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import io.github.micjabbour.androidguard.models.DeviceRegRequest;
import io.github.micjabbour.androidguard.models.DeviceRegResponse;
import io.github.micjabbour.androidguard.models.FcmTokenRequest;
import io.github.micjabbour.androidguard.models.FcmTokenResponse;
import io.github.micjabbour.androidguard.models.LocationUpdateRequest;
import io.github.micjabbour.androidguard.models.LocationUpdateResponse;
import io.github.micjabbour.androidguard.models.TestCredsResponse;
import io.github.micjabbour.androidguard.models.TestTokenResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Mike on 30/05/2017.
 */

public class NetworkService {
    private static String baseUrl = "https://androidguard.pythonanywhere.com/api/v1/";
    private NetworkAPI networkAPI;
    private OkHttpClient okHttpClient;
    private Context context; //used to access AppSettings (for device auth token)
    //initialized via constructor injection

    public NetworkService(Context context) { this(baseUrl, context); }
    public NetworkService(String baseUrl, Context context) {
        this.context = context;
        okHttpClient = buildClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        networkAPI = retrofit.create(NetworkAPI.class);
    }

    public NetworkAPI getAPI() { return networkAPI; }

    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response response = chain.proceed(chain.request());
                return response;
            }
        });
        builder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                requestBuilder.header("Accept", "application/json");
                //if the request does not contain an authorization header
                Log.d("NetworkService", "add auth interceptor");
                if(original.header("Authorization") == null) {
                    //add device token authorization header
                    AppSettings settings = new AppSettings(context);
                    Log.d("NetworkService", "add auth interceptor2");
                    if(!settings.getDeviceAuthToken().isEmpty()) {
                        Log.d("NetworkService", "add auth interceptor3");
                        String basic = Credentials.basic(settings.getDeviceAuthToken(), "");
                        requestBuilder.header("Authorization", basic);
                    }
                }
                requestBuilder.method(original.method(),original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        return builder.build();
    }

    public Observable<?> prepareObservable(Observable<?> unpreparedObservable) {
        //TODO: implement observable cache here
        return unpreparedObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public interface NetworkAPI {
        @GET("test_creds")
        Observable<Response<TestCredsResponse>> testCreds(@Header("Authorization") String credsBasicAuth);
        @GET("test_token")
        Observable<Response<TestTokenResponse>> testRequest();
        @POST("fcm_token")
        Observable<Response<FcmTokenResponse>> updateFcmToken(@Body FcmTokenRequest body);
        @POST("locations")
        Observable<Response<LocationUpdateResponse>> updateDeviceLocation(@Body LocationUpdateRequest body);
        @POST("devices")
        Observable<Response<DeviceRegResponse>> registerDevice(@Header("Authorization") String credsBasicAuth, @Body DeviceRegRequest body);
    }
}
