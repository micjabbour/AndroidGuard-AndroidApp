package io.github.micjabbour.androidguard.activities.status;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.micjabbour.androidguard.AndroidGuardApp;
import io.github.micjabbour.androidguard.AppSettings;
import io.github.micjabbour.androidguard.R;
import io.github.micjabbour.androidguard.activities.setup.SetupActivity;

public class StatusActvity extends AppCompatActivity implements StatusView {
    //presenter
    private StatusPresenter presenter;
    //UI references
    @BindView(R.id.status_progress)
    ProgressBar mProgressView;
    @BindView(R.id.status_layout)
    View mStatusLayoutView;
    @BindView(R.id.tv_status)
    TextView mStatusView;
    @BindView(R.id.tv_status_details)
    TextView mStatusDetailsView;
    @BindView(R.id.button_logout)
    Button mLogoutButton;
    @BindView(R.id.button_hide_show_app)
    Button mHideShowAppButton;
    @BindView(R.id.tv_secret_number)
    TextView mSecretNumberEditText;
    boolean appIsHidden = false;
    private static final String[] neededPermissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
    };

    final private int REQUEST_CODE_ASK_ALL_PERMISSIONS = 124;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);
        presenter = new StatusPresenterImpl(this, (AndroidGuardApp)getApplication());
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSettings settings = new AppSettings(StatusActvity.this);
                settings.clearAllAuthInfo();
                Intent intent = new Intent(StatusActvity.this, SetupActivity.class);
                finish();
                startActivity(intent);
            }
        });
        //check for status
        showProgress(true);
        checkStatusWrapper();
        appIsHidden = presenter.isAppHidden();

        mHideShowAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShowApp();
            }
        });
        mSecretNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if(id == R.id.hide || id == EditorInfo.IME_NULL) {
                    hideShowApp();
                    return true;
                }
                return false;
            }
        });
        resetHideShowButtonText();
        //set text on text edit to the previously configured secret number
        AppSettings settings = new AppSettings(this);
        mSecretNumberEditText.setText(settings.getSecretNumber());
    }

    private void hideShowApp() {
        if(!appIsHidden) {
            AppSettings settings = new AppSettings(this);
            settings.setSecretNumber(mSecretNumberEditText.getText().toString());
        }
        //reverse hide/show state
        presenter.showAppIcon(appIsHidden);
        appIsHidden= !appIsHidden;
        if(appIsHidden) finish();
    }

    private void resetHideShowButtonText() {
        if(appIsHidden)
            mHideShowAppButton.setText(getString(R.string.button_show));
        else
            mHideShowAppButton.setText(getString(R.string.button_hide));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.rxUnSubscribe();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mStatusLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
            mStatusLayoutView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mStatusLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mStatusLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void setStatus(Status s) {
        showProgress(false);
        switch(s) {
            case CONNECTED:
                mStatusView.setText(getString(R.string.status_ok));
                mStatusDetailsView.setText(getString(R.string.status_ok_details));
                mStatusView.setTextColor(ContextCompat.getColor(this, R.color.colorOk));
                break;
            case DISCONNECTED:
                mStatusView.setText(getString(R.string.status_disconnected));
                mStatusDetailsView.setText(getString(R.string.status_disconnected_details));
                mStatusView.setTextColor(ContextCompat.getColor(this, R.color.colorError));
                break;
            case UNKOWN_ERROR:
                mStatusView.setText(getString(R.string.status_unkown_error));
                mStatusDetailsView.setText(getString(R.string.status_unkown_error_details));
                mStatusView.setTextColor(ContextCompat.getColor(this, R.color.colorError));
                break;
            case AUTHENTICATION_ERROR:
                mStatusView.setText(getString(R.string.status_auth_problem));
                mStatusDetailsView.setText(getString(R.string.status_auth_problem_details));
                mStatusView.setTextColor(ContextCompat.getColor(this, R.color.colorError));
                mLogoutButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showMessageOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        StatusActvity.this.finish();
                    }
                })
                .create()
                .show();
    }


    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    private void checkStatusWrapper() {
        //check if permissions are not granted
        final List<String> permissionsList = new ArrayList<>();
        for(String permission : neededPermissions)
            addPermission(permissionsList, permission);
        //if there are any permissions that are not granted
        if (permissionsList.size() > 0) {
            //show message box to request permissions
            showMessageOK(getString(R.string.need_all_permissions),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //request permissions from user
                            ActivityCompat.requestPermissions(StatusActvity.this,
                                    permissionsList.toArray(new String[permissionsList.size()]),
                                    REQUEST_CODE_ASK_ALL_PERMISSIONS);
                        }
                    });
            return;
        }
        //if all permissions are already granted
        presenter.checkStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_ALL_PERMISSIONS:
            {
                //check if all permissions are granted
                for(int grantResult : grantResults) {
                    if(grantResult!= PackageManager.PERMISSION_GRANTED) {
                        // some permission is not granted
                        Toast.makeText(this, getString(R.string.error_permission_denied), Toast.LENGTH_SHORT)
                                .show();
                        finish();
                        return;
                    }
                }
                //if all permissions are granted, do check current status
                presenter.checkStatus();
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
