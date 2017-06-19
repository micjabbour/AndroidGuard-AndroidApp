package io.github.micjabbour.androidguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import io.github.micjabbour.androidguard.activities.LauncherActivity;

/**
 * Created by Mike on 31/05/2017.
 */

//inspiration from: https://stackoverflow.com/a/22754642

public class ShowHideApp {
    public static void showAppIcon(Context context, boolean show) {
        if(show){
            PackageManager p = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, LauncherActivity.class);
            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            // Start the app
            Intent i=new Intent(context,LauncherActivity.class);;
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to call startActivity() from outside of an Activity context
            context.startActivity(i);
        } else {
            PackageManager p = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, LauncherActivity.class);
            p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean isAppHidden(Context context) {
        PackageManager p = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, LauncherActivity.class);
        if(p.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            return true;
        else
            return false;
    }
}
