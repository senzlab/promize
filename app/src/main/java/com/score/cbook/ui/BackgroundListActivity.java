package com.score.cbook.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;

public class BackgroundListActivity extends BaseActivity implements View.OnClickListener {
    private ImageView lblue;
    private ImageView black;
    private ImageView wred;
    private ImageView purple;
    private ImageView green;
    private ImageView blue;
    private ImageView gray;
    private ImageView rose;
    private ImageView lpurple;
    private ImageView teal;
    private ImageView brown;
    private ImageView dpurple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.background_layout);

        setupToolbar();
        setupActionBar();
        initStickers();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.search_contacts_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Choose background");

        // back button
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initStickers() {
        lblue = (ImageView) findViewById(R.id.lblue);
        black = (ImageView) findViewById(R.id.black);
        wred = (ImageView) findViewById(R.id.wine_red);
        purple = (ImageView) findViewById(R.id.purple);
        green = (ImageView) findViewById(R.id.green);
        blue = (ImageView) findViewById(R.id.blue);
        gray = (ImageView) findViewById(R.id.gray);
        rose = (ImageView) findViewById(R.id.rose);
        lpurple = (ImageView) findViewById(R.id.lpurple);
        teal = (ImageView) findViewById(R.id.teal);
        brown = (ImageView) findViewById(R.id.brown);
        dpurple = (ImageView) findViewById(R.id.dpurple);

        lblue.setOnClickListener(this);
        black.setOnClickListener(this);
        wred.setOnClickListener(this);
        purple.setOnClickListener(this);
        green.setOnClickListener(this);
        blue.setOnClickListener(this);
        gray.setOnClickListener(this);
        rose.setOnClickListener(this);
        lpurple.setOnClickListener(this);
        teal.setOnClickListener(this);
        brown.setOnClickListener(this);
        dpurple.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == lblue) {
            selectSticker(R.color.lblue);
        } else if (v == black) {
            selectSticker(R.color.black);
        } else if (v == wred) {
            selectSticker(R.color.wine_red);
        } else if (v == purple) {
            selectSticker(R.color.purple);
        } else if (v == green) {
            selectSticker(R.color.android_green);
        } else if (v == blue) {
            selectSticker(R.color.blue);
        } else if (v == gray) {
            selectSticker(R.color.gray);
        } else if (v == rose) {
            selectSticker(R.color.rose_red);
        } else if (v == lpurple) {
            selectSticker(R.color.lpurple);
        } else if (v == teal) {
            selectSticker(R.color.teal);
        } else if (v == brown) {
            selectSticker(R.color.brown);
        } else if (v == dpurple) {
            selectSticker(R.color.dpurple);
        }
    }

    private void selectSticker(int colorId) {
        Intent intent = new Intent();
        intent.putExtra("COLOR", colorId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
