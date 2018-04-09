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

public class StickerListActivity extends BaseActivity implements View.OnClickListener {
    private ImageView flower;
    private ImageView heart;
    private ImageView baloon;
    private ImageView cheers;
    private ImageView gift;
    private ImageView rose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_layout);

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
        titleText.setText("Add sticker");

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
        heart = (ImageView) findViewById(R.id.heart);
        baloon = (ImageView) findViewById(R.id.baloons);
        cheers = (ImageView) findViewById(R.id.cheers);
        gift = (ImageView) findViewById(R.id.gift);
        rose = (ImageView) findViewById(R.id.rose);

        flower.setOnClickListener(this);
        heart.setOnClickListener(this);
        baloon.setOnClickListener(this);
        cheers.setOnClickListener(this);
        gift.setOnClickListener(this);
        rose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == flower) {
            selectSticker(R.drawable.flowers);
        } else if (v == heart) {
            selectSticker(R.drawable.heartw);
        } else if (v == baloon) {
            selectSticker(R.drawable.balloons);
        } else if (v == cheers) {
            selectSticker(R.drawable.cheers);
        } else if (v == gift) {
            selectSticker(R.drawable.gifti);
        } else if (v == rose) {
            selectSticker(R.drawable.rose);
        }
    }

    private void selectSticker(int stickerId) {
        Intent intent = new Intent();
        intent.putExtra("STICKER", stickerId);
        setResult(Activity.RESULT_OK, intent);
        finish();
        //overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }
}
