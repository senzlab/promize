package com.score.cbook.ui;

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

import com.score.cbook.R;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;

public class UsernameChangeActivity extends BaseActivity {

    private static final String TAG = UsernameChangeActivity.class.getName();

    private EditText current_username;
    private EditText new_username;

    private Button update_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usernme_change);

        initUi();
        // initPrefs();
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
        current_username = (EditText) findViewById(R.id.current_username);
        new_username = (EditText) findViewById(R.id.new_username);
        current_username.setTypeface(typeface, Typeface.NORMAL);
        new_username.setTypeface(typeface, Typeface.NORMAL);

        // buttons
        update_btn = (Button) findViewById(R.id.update_btn);
        update_btn.setTypeface(typeface, Typeface.BOLD);
        final Account useAccount = PreferenceUtil.getAccount(this);

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUsername = current_username.getText().toString().trim();
                String newUsername = new_username.getText().toString().trim();


                if (useAccount.getPhoneNo().equals(currentUsername)) {
                    try {
                        ActivityUtil.isValidUsername(currentUsername, newUsername);
                        useAccount.setPhoneNo(newUsername);
                        navigateToSettings(useAccount);
                    } catch (InvalidInputFieldsException e) {
                        e.printStackTrace();
                        displayInformationMessageDialog("Error", "Invalid new username");
                    }
                } else {
                    displayInformationMessageDialog("Error", "Invalid current username ");
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
        titleText.setText("Change Username");

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

    private void navigateToSettings(Account useAccount) {
        Intent intent = new Intent(UsernameChangeActivity.this, SettingsActivity.class);
        startActivity(intent);
        PreferenceUtil.updateUsernameAccount(this, useAccount);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
