package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.PreferenceUtil;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getName();

    private TextView phone;
    private TextView account;
    private TextView password;
    private TextView terms;
    private Button accBtn;
    private Button phnBtn;
    private Button passBtn;
    private Button termsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUi();
        initPrefs();
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

    private void initUi() {
        // text views
        phone = (TextView) findViewById(R.id.phone);
        account = (TextView) findViewById(R.id.account);
        password = (TextView) findViewById(R.id.promizeId);
        terms = (TextView) findViewById(R.id.terms);
        phone.setTypeface(typeface, Typeface.NORMAL);
        account.setTypeface(typeface, Typeface.NORMAL);
        password.setTypeface(typeface, Typeface.NORMAL);
        terms.setTypeface(typeface, Typeface.NORMAL);

        // buttons
        accBtn = (Button) findViewById(R.id.acc_btn);
        phnBtn = (Button) findViewById(R.id.phn_btn);
        passBtn = (Button) findViewById(R.id.pass_btn);
        termsBtn = (Button) findViewById(R.id.terms_btn);
        accBtn.setTypeface(typeface, Typeface.BOLD);
        phnBtn.setTypeface(typeface, Typeface.BOLD);
        passBtn.setTypeface(typeface, Typeface.BOLD);
        termsBtn.setTypeface(typeface, Typeface.BOLD);
    }

    private void initPrefs() {
        Account useAccount = PreferenceUtil.getAccount(this);

        if (useAccount.getAccountNo().isEmpty()) {
            account.setText("Account");
            accBtn.setText("Add");
        } else {
            account.setText("Account - " + useAccount.getAccountNo());
            accBtn.setText("CHANGE");
        }
        phone.setText("Phone - " + useAccount.getPhoneNo());
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Settings");

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

}