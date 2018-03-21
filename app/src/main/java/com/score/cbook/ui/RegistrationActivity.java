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
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = RegistrationActivity.class.getName();

    // ui controls
    private Button registerBtn;
    private TextView message;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Toolbar toolbar;

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
            if (msg != null && msg.equalsIgnoreCase("SUCCESS")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Registration success", Toast.LENGTH_LONG).show();
                // login success
                // save account
                // go to home
                PreferenceUtil.saveAccount(this, account);
                navigateToHome();
            } else if (msg != null && msg.equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Registration fail";
                displayInformationMessageDialog("ERROR", informationMessage);
            } else if (msg != null && msg.equalsIgnoreCase("VERIFICATION_FAIL")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Signature verification fail. Please contact sampath support regarding this issue";
                displayInformationMessageDialog("ERROR", informationMessage);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initUi() {
        message = (TextView) findViewById(R.id.welcome_message);
        editTextAccount = (EditText) findViewById(R.id.registering_user_id);
        editTextPassword = (EditText) findViewById(R.id.registering_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.registering_confirm_password);

        message.setTypeface(typeface, Typeface.BOLD);
        editTextAccount.setTypeface(typeface, Typeface.NORMAL);
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

    /**
     * Sign-up button action,
     * create user and validate fields from here
     */
    private void onClickRegister() {
        ActivityUtil.hideSoftKeyboard(this);

        // crate account
        final String accountNo = editTextAccount.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtil.isValidRegistrationFields(accountNo, password, confirmPassword);
            String confirmationMessage = "<font color=#636363>Are you sure you want to register with account </font> <font color=#F37920>" + "<b>" + accountNo + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                        ActivityUtil.showProgressDialog(RegistrationActivity.this, "Please wait...");
                        account = new Account();
                        account.setPhoneNo(accountNo);
                        account.setPassword(password);
                        doAuth();
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
        } catch (MisMatchFieldException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Mismatching password and confirm password");
        }
    }

    private void doAuth() {
        // send login senz
        try {
            String user = PreferenceUtil.getSenzieAddress(this);
            Senz senz = SenzUtil.authSenz(this, user);
            send(senz);
        } catch (NoUserException e) {
            e.printStackTrace();
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
