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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = RegistrationActivity.class.getName();

    // ui controls
    private Button registerBtn;
    private EditText editTextPhone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private String senzieAddress;
    private Account account;

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

    private void handleSenz(Senz senz) {
        if (senz.getAttributes().containsKey("status")) {
            String msg = senz.getAttributes().get("status");
            if (msg != null && msg.equalsIgnoreCase("REG_DONE")) {
                PreferenceUtil.saveSenzeisAddress(RegistrationActivity.this, senzieAddress);
                doAuth();
            } else if (msg != null && msg.equalsIgnoreCase("REG_ALR")) {
                doAuth();
            } else if (msg != null && msg.equalsIgnoreCase("SUCCESS")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Registration done", Toast.LENGTH_LONG).show();

                PreferenceUtil.saveAccount(this, account);
                navigateToHome();
            } else if (msg != null && msg.equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                displayInformationMessageDialog("ERROR", "Registration fail");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initUi();
        initToolbar();
        initActionBar();
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
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Register");

        // back button
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initUi() {
        editTextPhone = (EditText) findViewById(R.id.registering_user_id);
        editTextPassword = (EditText) findViewById(R.id.registering_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.registering_confirm_password);

        editTextPhone.setTypeface(typeface, Typeface.NORMAL);
        editTextPassword.setTypeface(typeface, Typeface.NORMAL);
        editTextConfirmPassword.setTypeface(typeface, Typeface.NORMAL);

        registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setTypeface(typeface, Typeface.BOLD);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickRegister();
            }
        });
    }

    private void onClickRegister() {
        ActivityUtil.hideSoftKeyboard(this);

        // crate account
        final String phone = editTextPhone.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtil.isValidRegistrationFields(phone, password, confirmPassword);
            String confirmationMessage = "<font color=#636363>Are you sure you want to register with phone no </font> <font color=#F37920>" + "<b>" + password + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                        ActivityUtil.showProgressDialog(RegistrationActivity.this, "Please wait...");
                        account = new Account();
                        account.setPhoneNo(phone);
                        account.setPassword(password);
                        doReg();
                    } else {
                        ActivityUtil.showCustomToastShort("No network connection", RegistrationActivity.this);
                    }
                }
            });
        } catch (InvalidAccountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Invalid phone no");
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Invalid password. Password should contains more than 4 characters");
        } catch (MisMatchFieldException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Mismatching password and confirm password");
        }
    }

    private void doReg() {
        try {
            // generate keypair
            // generate senzie address
            if (PreferenceUtil.getSenzieAddress(this).isEmpty()) {
                CryptoUtil.initKeys(this);
                senzieAddress = CryptoUtil.getSenzieAddress(this);
            }

            // share keys with zwitch
            sendSenz(SenzUtil.regSenz(this, senzieAddress));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAuth() {
        // share keys with auth
        String user = PreferenceUtil.getSenzieAddress(this);
        sendSenz(SenzUtil.authSenz(this, user));
    }

    private void navigateToHome() {
        Intent intent = new Intent(RegistrationActivity.this, DashBoardActivity.class);
        RegistrationActivity.this.startActivity(intent);
        RegistrationActivity.this.finish();
    }
}
