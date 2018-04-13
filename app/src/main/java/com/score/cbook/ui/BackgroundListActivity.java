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
    private ImageView flower;
    private ImageView heartj;
    private ImageView hearti;
    private ImageView baloon;
    private ImageView stars;
    private ImageView cheers;
    private ImageView newyear;
    private ImageView gift;
    private ImageView eyes;
    private ImageView rose;

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
        flower = (ImageView) findViewById(R.id.flowers);
        heartj = (ImageView) findViewById(R.id.heartsj);
        hearti = (ImageView) findViewById(R.id.heart);
        baloon = (ImageView) findViewById(R.id.baloons);
        stars = (ImageView) findViewById(R.id.stars);
        cheers = (ImageView) findViewById(R.id.cheers);
        newyear = (ImageView) findViewById(R.id.newyear);
        gift = (ImageView) findViewById(R.id.gift);
        eyes = (ImageView) findViewById(R.id.eyes);
        rose = (ImageView) findViewById(R.id.rose);

        flower.setOnClickListener(this);
        heartj.setOnClickListener(this);
        hearti.setOnClickListener(this);
        baloon.setOnClickListener(this);
        stars.setOnClickListener(this);
        cheers.setOnClickListener(this);
        newyear.setOnClickListener(this);
        gift.setOnClickListener(this);
        eyes.setOnClickListener(this);
        rose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == flower) {
            //selectSticker(R.drawable.flowers);
            // select camera
        } else if (v == heartj) {
            selectSticker(R.color.black);
        } else if (v == hearti) {
            selectSticker(R.color.indian_read);
        } else if (v == baloon) {
            selectSticker(R.color.dpurple);
        } else if (v == stars) {
            selectSticker(R.color.android_green);
        } else if (v == cheers) {
            selectSticker(R.color.blue);
        } else if (v == newyear) {
            selectSticker(R.color.gray);
        } else if (v == gift) {
            selectSticker(R.color.yellow);
        } else if (v == eyes) {
            selectSticker(R.color.teal);
        } else if (v == rose) {
            //selectSticker(R.drawable.rose);
        }
    }

    private void selectSticker(int colorId) {
        Intent intent = new Intent();
        intent.putExtra("COLOR", colorId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
