package io.github.micjabbour.androidguard.activities.setup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.micjabbour.androidguard.AndroidGuardApp;
import io.github.micjabbour.androidguard.AppSettings;
import io.github.micjabbour.androidguard.R;
import io.github.micjabbour.androidguard.activities.status.StatusActvity;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A login screen that offers login via email/password.
 */
public class SetupActivity extends AppCompatActivity implements SetupView {
    //presenter
    private SetupPresenter presenter;
    // UI references
    @BindView(R.id.username)
    EditText mUsernameView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.device_name)
    EditText mDeviceName;
    @BindView(R.id.register_device_button)
    Button mEmailSignInButton;
    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.tv_error)
    TextView mTextViewError;

    private static final String BACKGROUND_REQUEST_KEY = "BACKGROUND_REQUEST_KEY";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean backroundRequest = false;


    private static final String LOG_TAG = "SetupActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        presenter = new SetupPresenterImpl(this, (AndroidGuardApp)getApplication());
        mDeviceName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptDeviceRegistration();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDeviceRegistration();
            }
        });

        if(savedInstanceState!=null){
            backroundRequest = savedInstanceState.getBoolean(BACKGROUND_REQUEST_KEY);
            if(backroundRequest) attemptDeviceRegistration();
        }

        checkPlayServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(backroundRequest)
            attemptDeviceRegistration();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.rxUnSubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BACKGROUND_REQUEST_KEY, backroundRequest);
    }

    private ArrayList<String> ungrantedPermissions() {
        String[] allPermissions = {INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
        ArrayList<String> result = new ArrayList<>();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return result;
        for (String permission : allPermissions) {
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission);
        }
        return result;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptDeviceRegistration() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mTextViewError.setVisibility(View.INVISIBLE);

        // Store values at the time of the registration attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String deviceName = mDeviceName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid device name
        if(!presenter.isValidDeviceName(deviceName)) {
            mDeviceName.setError(getString(R.string.error_invalid_device_name));
            focusView = mDeviceName;
            cancel = true;
        }
        // Check for a valid password
        if (!presenter.isValidPassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid email address
        if (!presenter.isValidUsername(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (presenter.hasBackgroundRequests()) {
                Log.d("SetupActivity", "presenter has background requests.");
                return;
            }
            // Show a progress spinner, and perform registration attempt
            showProgress(true);
            backroundRequest = true;
            presenter.attemptDeviceRegistration(username, password, deviceName);
        }
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void setPasswordError(String error) {
        mPasswordView.setError(error);
    }
    public void setUsernameError(String error) {
        mUsernameView.setError(error);
    }

    @Override
    public void onRegistrationSuccess() {
        showProgress(false);
        backroundRequest= false;
        AppSettings settings = new AppSettings(this);
        settings.setCredsUsername(mUsernameView.getText().toString());
        settings.setCredsPassword(mPasswordView.getText().toString());
        settings.setDeviceName(mDeviceName.getText().toString());
        Intent intent = new Intent(this, StatusActvity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onRegistrationCredsError() {
        showProgress(false);
        backroundRequest = false;
        mTextViewError.setVisibility(View.VISIBLE);
        mTextViewError.setText(getString(R.string.error_register_creds_error));
    }
    @Override
    public void onRegistrationUnkownError() {
        showProgress(false);
        backroundRequest = false;
        mTextViewError.setVisibility(View.VISIBLE);
        mTextViewError.setText(getString(R.string.error_unknown_error));
    }
    @Override
    public void onRegistrationNetworkError() {
        showProgress(false);
        backroundRequest = false;
        mTextViewError.setVisibility(View.INVISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_network_error_title);
        builder.setMessage(R.string.error_network_error_message);
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SetupActivity.this.finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SetupActivity.this.finish();
            }
        });
        builder.show();
    }
    @Override
    public void onRegistrationDeviceNameAlreadyExists() {
        showProgress(false);
        backroundRequest = false;
        mTextViewError.setVisibility(View.INVISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning_device_exists_title);
        builder.setMessage(R.string.warning_device_exists_message);
        builder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onRegistrationSuccess();
            }
        });
        builder.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppSettings settings = new AppSettings(SetupActivity.this);
                settings.setDeviceAuthToken("");
                dialog.dismiss();
            }
        });
        builder.show();
    }
    @Override
    public String getStringFromResId(int resId) {
        return getString(resId);
    }

    //see https://stackoverflow.com/a/31016761/
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(this, getString(R.string.google_play_services_error), Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
}

