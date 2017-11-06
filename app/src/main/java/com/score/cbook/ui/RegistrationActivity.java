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
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.CryptoUtils;
import com.score.cbook.utils.NetworkUtil;
import com.score.cbook.utils.PreferenceUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

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
        ActivityUtils.hideSoftKeyboard(this);

        // crate user
        String account = editTextAccount.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtils.isValidRegistrationFields(account, password, confirmPassword);
            registeringUser = new User("0", account);
            String confirmationMessage = "<font color=#636363>Are you sure you want to register with account </font> <font color=#F37920>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                        ActivityUtils.showProgressDialog(RegistrationActivity.this, "Please wait...");
                        doRegistration();
                    } else {
                        ActivityUtils.showCustomToastShort("No network connection", RegistrationActivity.this);
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
            CryptoUtils.initKeys(this);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    /**
     * Create register senz
     * Send register senz to senz service via service binder
     */
    private void doRegistration() {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put(getResources().getString(R.string.time), ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put(getResources().getString(R.string.pubkey), PreferenceUtils.getRsaKey(this, CryptoUtils.PUBLIC_KEY));

        // new senz
        String id = "_ID";
        String signature = "";
        SenzTypeEnum senzType = SenzTypeEnum.SHARE;
        User sender = new User("", registeringUser.getUsername());
        User receiver = new User("", getResources().getString(R.string.switch_name));
        Senz senz = new Senz(id, signature, senzType, sender, receiver, senzAttributes);

        // sending senz to service
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
            // msg response received
            ActivityUtils.cancelProgressDialog();
            String msg = senz.getAttributes().get("status");
            if (msg != null && (msg.equalsIgnoreCase("REG_DONE") || msg.equalsIgnoreCase("REG_ALR"))) {
                Toast.makeText(this, "Successfully registered", Toast.LENGTH_LONG).show();
                // save user
                // navigate home
                PreferenceUtils.saveUser(this, registeringUser);
                PreferenceUtils.savePassword(this, editTextPassword.getText().toString().trim());
                navigateToHome();
            } else if (msg != null && msg.equalsIgnoreCase("REG_FAIL")) {
                String informationMessage = "<font size=10>Seems username </font> <font color=#F88F8C>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font> <font> already obtained by some other user, try a different username</font>";
                displayInformationMessageDialog("Registration fail", informationMessage);
            }
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        Intent intent = new Intent(RegistrationActivity.this, DrawerActivity.class);
        RegistrationActivity.this.startActivity(intent);
        RegistrationActivity.this.finish();
    }
}
