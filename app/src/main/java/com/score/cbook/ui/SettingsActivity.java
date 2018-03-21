package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.PreferenceUtil;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getName();

    private TextView phone;
    private TextView phoneV;
    private TextView account;
    private TextView accountV;
    private TextView promizeId;
    private TextView promizeIdV;

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
        phoneV = (TextView) findViewById(R.id.phonev);
        account = (TextView) findViewById(R.id.account);
        accountV = (TextView) findViewById(R.id.accountv);
        promizeId = (TextView) findViewById(R.id.promizeid);
        promizeIdV = (TextView) findViewById(R.id.promizeidV);
        phone.setTypeface(typeface, Typeface.NORMAL);
        phoneV.setTypeface(typeface, Typeface.NORMAL);
        account.setTypeface(typeface, Typeface.NORMAL);
        accountV.setTypeface(typeface, Typeface.NORMAL);
        promizeId.setTypeface(typeface, Typeface.NORMAL);
        promizeIdV.setTypeface(typeface, Typeface.NORMAL);
    }

    private void initPrefs() {
        try {
            String user = PreferenceUtil.getSenzieAddress(this);
            Account useAccount = PreferenceUtil.getAccount(this);

            accountV.setText(useAccount.getAccountNo());
            phoneV.setText(useAccount.getPhoneNo());
            promizeIdV.setText(user);
        } catch (NoUserException e) {
            e.printStackTrace();
        }
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
