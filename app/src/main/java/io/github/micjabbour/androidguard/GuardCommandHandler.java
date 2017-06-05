package io.github.micjabbour.androidguard;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import io.github.micjabbour.androidguard.services.LocationUpdateJobService;
import io.github.micjabbour.androidguard.services.WipeSdCardJobService;

/**
 * Created by Mike on 04/06/2017.
 */


//processes a given command, used by FCM firebase messaging service
//and by SmsReceiveHandler
public class GuardCommandHandler {
    private static String LOG_TAG = "GuardCommandHandler";

    public static void handleCommand(Context context, String command) {
        handleCommand(context, command, null);
    }

    //command: command to execute
    //phonenumber: phoneNumber to send result to (if it is NULL results will be sent to webservice)
    public static void handleCommand(Context context, String command, @Nullable String phoneNumber) {
        switch(command) {
            case "getloc":
                if(phoneNumber != null)
                    LocationUpdateJobService.scheduleForSms(context, phoneNumber);
                else
                    LocationUpdateJobService.reschedule(context);
                break;
            case "showapp":
                ShowHideApp.showAppIcon(context, true);
                break;
            case "wipesdcard":
                WipeSdCardJobService.reschedule(context);
                break;
        }
    }
}
