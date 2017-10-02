package com.score.rahasak.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.utils.ImageUtils;
import com.score.senzc.pojos.Senz;

public class ChequePreviewActivity extends BaseActivity {

    private static final String TAG = NewChequeActivity.class.getName();

    // ui controls
    private ImageView chqueImg;
    private Cheque cheque;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                handleSenz(senz);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheque_preview_activity_layout);

        initPrefs();
        initCheque();
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
    }

    private void initPrefs() {
        this.cheque = getIntent().getParcelableExtra("cheque");
    }

    private void initCheque() {
        chqueImg = (ImageView) findViewById(R.id.cheque_preview);

        // sign cheque
        Bitmap chq = ImageUtils.loadImg(this, "chq.jpg");
        Bitmap sig = ImageUtils.loadImg(this, "sign.png");

        // sign cheque
        // add text
        Bitmap sChq = ImageUtils.addSign(chq, sig);

        // compress
        byte[] bytes = ImageUtils.bmpToBytes(sChq);
        byte[] compBytes = ImageUtils.compressImage(bytes);

        // set cheque
        cheque.setImg(Base64.encodeToString(compBytes, Base64.DEFAULT));

        Bitmap cChq = ImageUtils.bytesToBmp(compBytes);
        chqueImg.setImageBitmap(cChq);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.cheque_preview_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);

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

    private void handleSenz(Senz senz) {

    }
}
