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
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class AccountVerifyActivity extends BaseActivity {

    // UI fields
    private TextView hi;
    private TextView message;

    private String account;

    private static final String TAG = AccountVerifyActivity.class.getName();

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
                // reset account state
                // save account
                // navigate to salt confirm
                PreferenceUtil.saveAccountState(this, "PENDING");
                PreferenceUtil.saveAccountNo(this, account);
                navigateToConfirm();
            } else if (msg != null && msg.equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                displayInformationMessageDialog("ERROR", "Fail to verify account");
            } else if (msg != null && msg.equalsIgnoreCase("VERIFICATION_FAIL")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Verification fail. Please contact sampath support regarding this issue";
                displayInformationMessageDialog("ERROR", informationMessage);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vishwa_confirm);

        initUi();
        initToolbar();
        initActionBar();
        initPrefs();
    }

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

    private void initUi() {
        hi = (TextView) findViewById(R.id.hi_message);
        message = (TextView) findViewById(R.id.welcome_message);
        hi.setTypeface(typeface, Typeface.NORMAL);
        message.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.yes);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.showProgressDialog(AccountVerifyActivity.this, "Please wait...");
                verifyAccount();
            }
        });

        Button no = (Button) findViewById(R.id.no);
        no.setTypeface(typeface, Typeface.BOLD);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // exit
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Verify account");

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

    private void initPrefs() {
        if (getIntent().getExtras() != null)
            account = getIntent().getExtras().getString("ACCOUNT");
    }

    private void verifyAccount() {
        Senz senz = SenzUtil.accountSenz(this, account);
        sendSenz(senz);
    }

    private void navigateToConfirm() {
        Intent intent = new Intent(AccountVerifyActivity.this, SaltConfirmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

}

