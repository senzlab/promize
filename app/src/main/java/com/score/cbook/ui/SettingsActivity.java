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
    private TextView about;
    private Button accBtn;
    private Button phnBtn;
    private Button passChangeBtn;
    private Button passResetBtn;
    private Button termsBtn;
    private Button aboutBtn;

    private Account userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUi();
        initPrefs();
        initToolbar();
        initActionBar();
    }

    private void initUi() {
        // text views
        account = (TextView) findViewById(R.id.account);
        accountv = (TextView) findViewById(R.id.accountv);
        phone = (TextView) findViewById(R.id.phone);
        phonev = (TextView) findViewById(R.id.phonev);
        password = (TextView) findViewById(R.id.promizeId);
        terms = (TextView) findViewById(R.id.terms);
        about = (TextView) findViewById(R.id.about);
        phone.setTypeface(typeface, Typeface.NORMAL);
        phonev.setTypeface(typeface, Typeface.NORMAL);
        accountv.setTypeface(typeface, Typeface.NORMAL);
        account.setTypeface(typeface, Typeface.NORMAL);
        password.setTypeface(typeface, Typeface.NORMAL);
        terms.setTypeface(typeface, Typeface.NORMAL);
        about.setTypeface(typeface, Typeface.NORMAL);

        accBtn = (Button) findViewById(R.id.acc_btn);
        accBtn.setTypeface(typeface, Typeface.BOLD);
        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddAccount();
            }
        });

        phnBtn = (Button) findViewById(R.id.phn_btn);
        phnBtn.setTypeface(typeface, Typeface.BOLD);
        phnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUsernameChange();
            }
        });

        passChangeBtn = (Button) findViewById(R.id.pass_change_btn);
        passChangeBtn.setTypeface(typeface, Typeface.BOLD);
        passChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPasswordChange();
            }
        });

        passResetBtn = (Button) findViewById(R.id.pass_reset_btn);
        passResetBtn.setTypeface(typeface, Typeface.BOLD);
        passResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPasswordReset();
            }
        });

        termsBtn = (Button) findViewById(R.id.terms_btn);
        termsBtn.setTypeface(typeface, Typeface.BOLD);
        termsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToTerms();
            }
        });

        aboutBtn = (Button) findViewById(R.id.about_btn);
        aboutBtn.setTypeface(typeface, Typeface.BOLD);
        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAbout();
            }
        });
    }

    private void initPrefs() {
        userAccount = PreferenceUtil.getAccount(this);

        if (userAccount.getAccountNo().isEmpty()) {
            account.setText("Account");
            accBtn.setText("Add");
        } else if (userAccount.getState().equalsIgnoreCase("PENDING")) {
            account.setText("Account " + userAccount.getAccountNo());
            accBtn.setText("VERIFY");
        } else {
            account.setText("Account " + userAccount.getAccountNo());
            accBtn.setText("CHANGE");
        }
        phonev.setText(userAccount.getUsername());
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

    private void navigateToAddAccount() {
        if (userAccount.getAccountNo().isEmpty()) {
            // account verify
            Intent intent = new Intent(this, AddAccountInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.stay_in);
            finish();
        } else if (userAccount.getState().equalsIgnoreCase("PENDING")) {
            // salt confirm
            Intent intent = new Intent(this, SaltConfirmInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.stay_in);
            finish();
        } else {
            Intent intent = new Intent(SettingsActivity.this, AddAccountInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.stay_in);
            finish();
        }
    }

    private void navigateToUsernameChange() {
        Intent intent = new Intent(SettingsActivity.this, UsernameChangeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

    private void navigateToPasswordChange() {
        Intent intent = new Intent(SettingsActivity.this, PasswordChangeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

    private void navigateToPasswordReset() {
        Intent intent = new Intent(SettingsActivity.this, PasswordResetInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

    private void navigateToTerms() {
        Intent intent = new Intent(SettingsActivity.this, TermsOfUseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

    private void navigateToAbout() {

    }

}
