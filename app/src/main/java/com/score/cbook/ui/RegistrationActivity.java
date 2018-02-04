package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.PasswordMisMatchException;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = RegistrationActivity.class.getName();

    // ui controls
    private Button registerBtn;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private User registeringUser;
    private Toolbar toolbar;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                handleSenz(senz);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initUi();
        doPreRegistration();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "Bind to senz service");
        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from service
        if (isServiceBound) {
            Log.d(TAG, "Unbind to senz service");
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.registration_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initUi() {
        editTextAccount = (EditText) findViewById(R.id.registering_user_id);
        editTextPassword = (EditText) findViewById(R.id.registering_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.registering_confirm_password);

        editTextAccount.setTypeface(typeface, Typeface.BOLD);
        editTextPassword.setTypeface(typeface, Typeface.BOLD);
        editTextConfirmPassword.setTypeface(typeface, Typeface.BOLD);

        registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setTypeface(typeface, Typeface.BOLD);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                    onClickRegister();
                } else {
                    Toast.makeText(RegistrationActivity.this, "No network connectivity", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Sign-up button action,
     * create user and validate fields from here
     */
    private void onClickRegister() {
        ActivityUtil.hideSoftKeyboard(this);

        // crate user
        String account = editTextAccount.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtil.isValidRegistrationFields(account, password, confirmPassword);
            registeringUser = new User("0", account);
            String confirmationMessage = "<font color=#636363>Are you sure you want to register with account </font> <font color=#F37920>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                        ActivityUtil.showProgressDialog(RegistrationActivity.this, "Please wait...");
                        doRegistration();
                    } else {
                        ActivityUtil.showCustomToastShort("No network connection", RegistrationActivity.this);
                    }
                }
            });
        } catch (InvalidAccountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Invalid Account no. Account no should be 12 character length");
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Invalid password. Password should contains more than 4 characters");
        } catch (PasswordMisMatchException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Mismatching password and confirm password");
        }
    }

    /**
     * Create user
     * First initialize key pair
     * start service
     * bind service
     */
    private void doPreRegistration() {
        try {
            CryptoUtil.initKeys(this);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create register senz
     * Send register senz to senz service via service binder
     */
    private void doRegistration() {
        // senz reg senz
        Senz senz = SenzUtil.regSenz(this, registeringUser);
        send(senz);
    }

    private void doLogin() {
        // send login senz
        String password = editTextPassword.getText().toString().trim();
        Senz senz = SenzUtil.loginSenz(this, registeringUser, password);
        send(senz);
    }

    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param senz intent
     */
    private void handleSenz(Senz senz) {
        if (senz.getAttributes().containsKey("status")) {
            String msg = senz.getAttributes().get("status");
            if (msg != null && (msg.equalsIgnoreCase("REG_DONE") || msg.equalsIgnoreCase("REG_ALR"))) {
                // reg success
                // do login
                doLogin();
            } else if (msg != null && msg.equalsIgnoreCase("LOGIN_SUCCESS")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show();

                // login success
                // go to home
                PreferenceUtil.saveUser(this, registeringUser);
                PreferenceUtil.savePassword(this, editTextPassword.getText().toString().trim());
                navigateToHome();
            } else if (msg != null && msg.equalsIgnoreCase("REG_FAIL")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Invalid account, please make sure account <font size=10>Seems account no </font> <font color=#F37920>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font> <font> is correct</font>";
                displayInformationMessageDialog("Registration fail", informationMessage);
            } else if (msg != null && msg.equalsIgnoreCase("LOGIN_FAIL")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Your account no and password are mismatching. Please enter correct account no and password</font>";
                displayInformationMessageDialog("Login fail", informationMessage);
            }
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        Intent intent = new Intent(RegistrationActivity.this, DashBoardActivity.class);
        RegistrationActivity.this.startActivity(intent);
        RegistrationActivity.this.finish();
    }
}
