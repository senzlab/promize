package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.util.PreferenceUtil;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class TermsOfUseActivity extends BaseActivity {

    // UI fields
    private TextView hi;
    private TextView message;
    private RelativeLayout buttonPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_of_use_activity);

        initUi();
        initToolbar();
        initActionBar();
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        hi = (TextView) findViewById(R.id.hi_message);
        message = (TextView) findViewById(R.id.welcome_message);
        hi.setTypeface(typeface, Typeface.NORMAL);
        message.setTypeface(typeface, Typeface.NORMAL);

        buttonPanel = (RelativeLayout) findViewById(R.id.button_panel);
        if (PreferenceUtil.getAccount(this).getPhoneNo().isEmpty()) {
            // means not registered yet
            buttonPanel.setVisibility(View.VISIBLE);
        } else {
            buttonPanel.setVisibility(View.GONE);
        }

        Button yes = (Button) findViewById(R.id.yes);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegistration();
            }
        });

        Button no = (Button) findViewById(R.id.no);
        no.setTypeface(typeface, Typeface.BOLD);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TermsOfUseActivity.this.finish();
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
        titleText.setText("Terms of use");

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

    public void navigateToRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        TermsOfUseActivity.this.finish();
    }

}

