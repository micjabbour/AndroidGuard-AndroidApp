package io.github.micjabbour.androidguard.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.github.micjabbour.androidguard.AppSettings;
import io.github.micjabbour.androidguard.R;
import io.github.micjabbour.androidguard.activities.setup.SetupActivity;
import io.github.micjabbour.androidguard.activities.status.StatusActvity;

//an activity with no layout, responsible to launch the correct activity
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        AppSettings settings = new AppSettings(this);
        String deviceToken = settings.getDeviceAuthToken();
        Intent intent;
        if(deviceToken.equals(""))
            intent = new Intent(this, SetupActivity.class);
        else
            intent = new Intent(this, StatusActvity.class);
        startActivity(intent);
        finish();
    }
}
