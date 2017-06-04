package io.github.micjabbour.androidguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiveHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if an SMS message is being received
        if(intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage smsMessage = getMessagesFromIntentCompat(intent);
            //TODO: check for sms commands here
        }
    }

    //see https://stackoverflow.com/a/31816340
    //see https://developer.android.com/reference/android/telephony/SmsMessage.html#createFromPdu(byte[], java.lang.String)
    private SmsMessage getMessagesFromIntentCompat(Intent intent){
        SmsMessage smsMessage;

        if (Build.VERSION.SDK_INT >= 19) { //KITKAT
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            smsMessage = msgs[0];
        } else {
            Bundle extras = intent.getExtras();
            Object pdus[] = (Object[]) extras.get("pdus");
            smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
        }
        return smsMessage;
    }

}
