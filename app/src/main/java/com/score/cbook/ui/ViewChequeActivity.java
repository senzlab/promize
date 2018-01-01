package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.PhoneBookUtil;
import com.score.cbook.utils.SenzUtils;
import com.score.cbook.utils.TimeUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class ViewChequeActivity extends BaseActivity {

    private static final String TAG = ViewChequeActivity.class.getName();

    // ui controls
    private EditText userEditText;
    private EditText accountEditText;
    private EditText amountEditText;
    private EditText dateEditText;

    private RelativeLayout depositLayout;
    private RelativeLayout transferLayout;
    private RelativeLayout viewLayout;

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
        setContentView(R.layout.view_cheque_activity_layout);

        initUi();
        initCheque();
        initToolbar();
        initActionBar();
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
        userEditText = (EditText) findViewById(R.id.view_cheque_username);
        userEditText.setTypeface(typeface, Typeface.NORMAL);

        accountEditText = (EditText) findViewById(R.id.view_cheque_account);
        accountEditText.setTypeface(typeface, Typeface.NORMAL);

        amountEditText = (EditText) findViewById(R.id.view_cheque_amount);
        amountEditText.setTypeface(typeface, Typeface.NORMAL);

        dateEditText = (EditText) findViewById(R.id.view_cheque_date);
        dateEditText.setTypeface(typeface, Typeface.NORMAL);

        depositLayout = (RelativeLayout) findViewById(R.id.deposit_l);
        transferLayout = (RelativeLayout) findViewById(R.id.transfer_l);
        viewLayout = (RelativeLayout) findViewById(R.id.view_l);

        TextView depositT = (TextView) findViewById(R.id.deposit_t);
        depositT.setTypeface(typeface);

        TextView viewT = (TextView) findViewById(R.id.view_t);
        viewT.setTypeface(typeface);

        TextView transferT = (TextView) findViewById(R.id.transfer_t);
        transferT.setTypeface(typeface);

        ImageView deposit = (ImageView) findViewById(R.id.deposit);
        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.showProgressDialog(ViewChequeActivity.this, "Depositing...");

                Long timestamp = System.currentTimeMillis() / 1000;
                Senz senz = SenzUtils.getDepositChequeSenz(ViewChequeActivity.this, cheque, timestamp);
                send(senz);
            }
        });

        ImageView transfer = (ImageView) findViewById(R.id.transfer);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageView view = (ImageView) findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.hideSoftKeyboard(ViewChequeActivity.this);

                // cheque preview
                Intent intent = new Intent(ViewChequeActivity.this, ChequePActivity.class);
                intent.putExtra("UID", cheque.getUid());
                startActivity(intent);
            }
        });
    }

    private void initCheque() {
        cheque = getIntent().getParcelableExtra("CHEQUE");

        // update viewed state
        if (!cheque.isViewed()) {
            cheque.setViewed(true);
            ChequeSource.markChequeViewed(this, cheque.getUid());
        }

        userEditText.setText(PhoneBookUtil.getContactName(this, cheque.getUser().getPhone()));
        accountEditText.setText(cheque.getUser().getUsername());
        amountEditText.setText("Rs " + cheque.getAmount() + ".00");
        dateEditText.setText(cheque.getDate());

        // enable/disable deposit based on cheque owner
        if (cheque.isMyCheque()) {
            depositLayout.setVisibility(View.GONE);
            transferLayout.setVisibility(View.GONE);
        } else {
            if (cheque.getChequeState() == ChequeState.TRANSFER) {
                depositLayout.setVisibility(View.VISIBLE);
                transferLayout.setVisibility(View.VISIBLE);
            } else {
                depositLayout.setVisibility(View.GONE);
                transferLayout.setVisibility(View.GONE);
            }
        }
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.view_cheque_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        TextView timeText = (TextView) findViewById(R.id.time);
        titleText.setTypeface(typeface, Typeface.BOLD);
        timeText.setTypeface(typeface, Typeface.BOLD);
        if (cheque.isMyCheque()) {
            titleText.setText("To: " + PhoneBookUtil.getContactName(this, cheque.getUser().getPhone()));
        } else {
            titleText.setText("From: " + PhoneBookUtil.getContactName(this, cheque.getUser().getPhone()));
        }

        timeText.setText(TimeUtils.getTimeInWords(cheque.getTimestamp()));

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
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(ViewChequeActivity.this, "Deposit success", Toast.LENGTH_LONG).show();
                ViewChequeActivity.this.finish();

                // update cheque status in db
                ChequeSource.updateChequeState(this, "DEPOSIT", cheque.getUid());
            }
        }
    }
}
