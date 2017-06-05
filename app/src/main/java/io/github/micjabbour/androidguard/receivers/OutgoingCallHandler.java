package io.github.micjabbour.androidguard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.micjabbour.androidguard.AppSettings;
import io.github.micjabbour.androidguard.ShowHideApp;
import io.github.micjabbour.androidguard.activities.LauncherActivity;


//responsible to launch the Status Activity when dialing the secret number
//inspiration from:
// https://stackoverflow.com/a/19951892
// https://stackoverflow.com/a/23309191

public class OutgoingCallHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract phone number reformatted by previous receivers
        String phoneNumber = getResultData();
        if (phoneNumber == null) {
            // No reformatted number, use the original
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }

        AppSettings settings = new AppSettings(context);
        if(phoneNumber.equals(settings.getSecretNumber())){ // DialedNumber checking.
            // app will bring up, so cancel the broadcast
            setResultData(null);
            // application icon
            ShowHideApp.showAppIcon(context, true);
        }
    }
}
