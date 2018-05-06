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
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SaltConfirmActivity extends BaseActivity {

    private static final String TAG = SaltConfirmActivity.class.getName();

    private EditText amount;
    private int retry = 0;

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

                // set account state as verified
                PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_STATE, "VERIFIED");
                Toast.makeText(this, "Your account has been verified", Toast.LENGTH_LONG).show();
                SaltConfirmActivity.this.finish();
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
        setContentView(R.layout.salt_confirm_activity);

        initUi();
        initToolbar();
        initActionBar();
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
        amount = (EditText) findViewById(R.id.amount);
        amount.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.register_btn);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retry >= 3) {
                    displayInformationMessageDialog("ERROR", "Maximum no of retry attempts exceeded");
                } else {
                    ActivityUtil.showProgressDialog(SaltConfirmActivity.this, "Please wait...");
                    confirmSalt();
                }
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
        titleText.setText("Confirm account");

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

    private void confirmSalt() {
        String salt = amount.getText().toString().trim();
        Senz senz = SenzUtil.saltSenz(this, salt);
        sendSenz(senz);
        retry++;
    }

}

