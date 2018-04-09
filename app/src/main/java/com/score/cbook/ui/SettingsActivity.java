package com.score.cbook.ui;

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
import com.score.cbook.pojo.Account;
import com.score.cbook.util.PreferenceUtil;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getName();

    private TextView account;
    private TextView accountv;
    private TextView phone;
    private TextView phonev;
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
        account = (TextView) findViewById(R.id.account);
        accountv = (TextView) findViewById(R.id.accountv);
        phone = (TextView) findViewById(R.id.phone);
        phonev = (TextView) findViewById(R.id.phonev);
        password = (TextView) findViewById(R.id.promizeId);
        terms = (TextView) findViewById(R.id.terms);
        phone.setTypeface(typeface, Typeface.NORMAL);
        phonev.setTypeface(typeface, Typeface.NORMAL);
        accountv.setTypeface(typeface, Typeface.NORMAL);
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

        Button passBtn = (Button) findViewById(R.id.pass_btn);
        passBtn.setTypeface(typeface, Typeface.BOLD);
        passBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTopasswordChange();
            }
        });

        Button phnBtn = (Button) findViewById(R.id.phn_btn);
        phnBtn.setTypeface(typeface, Typeface.BOLD);
        phnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUsernameChange();
            }
        });

        Button accBtn = (Button) findViewById(R.id.acc_btn);
        accBtn.setTypeface(typeface, Typeface.BOLD);
        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddAccount();
            }
        });

    }

    private void initPrefs() {
        Account useAccount = PreferenceUtil.getAccount(this);

        if (useAccount.getAccountNo().isEmpty()) {
            accBtn.setVisibility(View.VISIBLE);
            accountv.setVisibility(View.GONE);
            //account.setText("Account");
            //accBtn.setText("Add");
        } else {
            accBtn.setVisibility(View.GONE);
            accountv.setVisibility(View.VISIBLE);
            accountv.setText(useAccount.getAccountNo());
            //account.setText("Account - " + useAccount.getAccountNo());
            //accBtn.setText("CHANGE");
        }
        phonev.setText(useAccount.getPhoneNo());
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

    private void navigateTopasswordChange() {
        Intent intent = new Intent(SettingsActivity.this, PasswordChangeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

    private void navigateToUsernameChange() {
        Intent intent = new Intent(SettingsActivity.this, UsernameChangeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

    private void navigateToAddAccount() {
        Intent intent = new Intent(SettingsActivity.this, BankTypeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }


}
