package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class TermsOfUseActivity extends BaseActivity {


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
        ((TextView) findViewById(R.id.message2)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message3)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message4)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message5)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message6)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message7)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message8)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message9)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message10)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message11)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message12)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.message13)).setTypeface(typeface, Typeface.BOLD);
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

}

