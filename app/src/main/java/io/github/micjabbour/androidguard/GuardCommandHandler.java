package io.github.micjabbour.androidguard;

import android.content.Context;
import android.support.annotation.Nullable;

import io.github.micjabbour.androidguard.services.LocationUpdateJobService;

/**
 * Created by Mike on 04/06/2017.
 */


//processes a given command, used by FCM firebase messaging service
//and by SmsReceiveHandler
public class GuardCommandHandler {
    public static void handleCommand(Context context, String command) {
        handleCommand(context, command, null);
    }

    //command: command to execute
    //phonenumber: phoneNumber to send result to (if it is NULL results will be sent to webservice)
    public static void handleCommand(Context context, String command, @Nullable String phoneNumber) {
        switch(command) {
            case "getloc":
                LocationUpdateJobService.reschedule(context);
                break;
        }
    }
}
