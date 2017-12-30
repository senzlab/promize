package com.score.cbook.ui;

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
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class ViewChequeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ViewChequeActivity.class.getName();

    // ui controls
    private EditText userEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private Button preview;
    private Button deposit;
    private Button transfer;

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
        amountEditText = (EditText) findViewById(R.id.view_cheque_amount);
        dateEditText = (EditText) findViewById(R.id.view_cheque_date);

        userEditText.setTypeface(typeface, Typeface.BOLD);
        amountEditText.setTypeface(typeface, Typeface.BOLD);
        dateEditText.setTypeface(typeface, Typeface.BOLD);

        preview = (Button) findViewById(R.id.view_cheque_send);
        preview.setTypeface(typeface, Typeface.BOLD);
        preview.setOnClickListener(this);

        deposit = (Button) findViewById(R.id.view_cheque_deposit);
        deposit.setTypeface(typeface, Typeface.BOLD);
        deposit.setOnClickListener(this);

        transfer = (Button) findViewById(R.id.view_cheque_transfer);
        transfer.setTypeface(typeface, Typeface.BOLD);
        transfer.setOnClickListener(this);
    }

    private void initCheque() {
        cheque = getIntent().getParcelableExtra("CHEQUE");

        // update viewed state
        if (!cheque.isViewed()) {
            cheque.setViewed(true);
            ChequeSource.markChequeViewed(this, cheque.getUid());
        }

        userEditText.setText(PhoneBookUtil.getContactName(this, cheque.getUser().getPhone()));
        amountEditText.setText("Rs " + cheque.getAmount());
        dateEditText.setText(cheque.getDate());

        // enable/disable deposit based on cheque owner
        if (cheque.isMyCheque()) {
            deposit.setVisibility(View.GONE);
            transfer.setVisibility(View.GONE);
        } else {
            if (cheque.getChequeState() == ChequeState.TRANSFER) {
                deposit.setVisibility(View.VISIBLE);
                transfer.setVisibility(View.VISIBLE);
            } else {
                deposit.setVisibility(View.GONE);
                transfer.setVisibility(View.GONE);
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
        titleText.setTypeface(typeface, Typeface.BOLD);
        if (cheque.isMyCheque()) {
            titleText.setText("Sent cheque");
        } else {
            titleText.setText("Received cheque");
        }

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

    @Override
    public void onClick(View v) {
        if (v == preview) {
            ActivityUtils.hideSoftKeyboard(this);

            // cheque preview
            Intent intent = new Intent(this, ChequePActivity.class);
            intent.putExtra("UID", cheque.getUid());
            startActivity(intent);
        } else if (v == deposit) {
            ActivityUtils.showProgressDialog(this, "Depositing...");

            Long timestamp = System.currentTimeMillis() / 1000;
            Senz senz = SenzUtils.getDepositChequeSenz(this, cheque, timestamp);
            send(senz);
        }
    }
}
