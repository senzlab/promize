package com.score.rahasak.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import com.score.rahasak.R;
import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.pojo.ChequeUser;
import com.score.rahasak.utils.ActivityUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.senzc.pojos.Senz;

public class NewChequeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = NewChequeActivity.class.getName();

    // ui controls
    private EditText user;
    private EditText amount;
    private EditText date;
    private Button send;

    private ChequeUser chequeUser;

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
        setContentView(R.layout.new_cheque_activity_layout);

        initPrefs();
        initUi();
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
        this.chequeUser = getIntent().getParcelableExtra("USER");
    }

    private void initUi() {
        user = (EditText) findViewById(R.id.new_cheque_username);
        amount = (EditText) findViewById(R.id.new_cheque_amount);
        date = (EditText) findViewById(R.id.new_cheque_date);

        user.setTypeface(typeface, Typeface.BOLD);
        amount.setTypeface(typeface, Typeface.BOLD);
        date.setTypeface(typeface, Typeface.BOLD);

        user.setText(PhoneBookUtil.getContactName(this, chequeUser.getPhone()));

        send = (Button) findViewById(R.id.new_cheque_send);
        send.setOnClickListener(this);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.new_cheque_header, null));
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

    private void onClickPreview() {
        // create cheque
        Cheque cheque = new Cheque();
        cheque.setUser(chequeUser);
        cheque.setAmount(Integer.parseInt(amount.getText().toString()));
        cheque.setDate(date.getText().toString().trim());

        // cheque preview
        Intent intent = new Intent(this, ChequePreviewActivity.class);
        intent.putExtra("CHEQUE", cheque);
        startActivity(intent);
        NewChequeActivity.this.finish();
    }

    private void handleSenz(Senz senz) {

    }

    @Override
    public void onClick(View v) {
        if (v == send) {
            ActivityUtils.hideSoftKeyboard(this);
            onClickPreview();
        }
    }
}
