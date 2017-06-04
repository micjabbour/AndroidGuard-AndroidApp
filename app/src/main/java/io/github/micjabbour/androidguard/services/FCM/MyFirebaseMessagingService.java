package io.github.micjabbour.androidguard.services.FCM;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.github.micjabbour.androidguard.GuardCommandHandler;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "FCMMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String command = remoteMessage.getData().get("command");
            GuardCommandHandler.handleCommand(this, command);
        }

    }
}
