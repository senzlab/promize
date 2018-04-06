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
import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.pojo.Bank;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class RedeemActivity extends BaseActivity {

    private static final String TAG = RedeemActivity.class.getName();

    // ui controls
    private Button done;
    private EditText editTextBank;
    private EditText editTextAccount;
    private EditText editTextConfirmAccount;
    private Toolbar toolbar;

    private Bank bank;
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

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(RedeemActivity.this, "Successfully redeemed the iGift", Toast.LENGTH_LONG).show();
                RedeemActivity.this.finish();

                // update cheque status in db
                ChequeSource.updateChequeState(this, cheque.getUid(), ChequeState.DEPOSIT);
            } else if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(RedeemActivity.this, "Failed to redeem iGift", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redeem_layout);

        initUi();
        initPrefs();
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

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Redeem iGift");

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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initUi() {
        editTextBank = (EditText) findViewById(R.id.bank);
        editTextAccount = (EditText) findViewById(R.id.account);
        editTextConfirmAccount = (EditText) findViewById(R.id.confirm_account);

        editTextBank.setTypeface(typeface, Typeface.BOLD);
        editTextAccount.setTypeface(typeface, Typeface.BOLD);
        editTextConfirmAccount.setTypeface(typeface, Typeface.BOLD);

        done = (Button) findViewById(R.id.done);
        done.setTypeface(typeface, Typeface.BOLD);
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickDone();
            }
        });
    }

    private void initPrefs() {
        this.bank = getIntent().getParcelableExtra("BANK");
        this.cheque = getIntent().getParcelableExtra("CHEQUE");

        editTextBank.setText(this.bank.getBankName());
    }

    private void onClickDone() {
        ActivityUtil.hideSoftKeyboard(this);

        // crate account
        final String accountNo = editTextAccount.getText().toString().trim();
        final String confirmAccountNo = editTextConfirmAccount.getText().toString().trim();
        try {
            ActivityUtil.isValidRedeemFileds(accountNo, confirmAccountNo);
            String confirmationMessage = "<font color=#636363>Are you sure you want to redeem the iGift for account </font> <font color=#F37920>" + "<b>" + accountNo + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RedeemActivity.this)) {
                        ActivityUtil.showProgressDialog(RedeemActivity.this, "Please wait...");
                        Senz senz = SenzUtil.redeemSenz(RedeemActivity.this, cheque, accountNo);
                        sendSenz(senz);
                    } else {
                        ActivityUtil.showCustomToastShort("No network connection", RedeemActivity.this);
                    }
                }
            });
        } catch (InvalidAccountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Mismatching account number");
        } catch (InvalidInputFieldsException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Account number should be 12 character length and start with 0 or 1");
        }
    }

}
