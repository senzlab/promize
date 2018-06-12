package com.score.cbook.ui;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.async.SenzPublisher;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.interfaces.ISenzPublisherListener;
import com.score.cbook.pojo.Bank;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;

public class RedeemActivity extends BaseActivity implements ISenzPublisherListener {

    private static final String TAG = RedeemActivity.class.getName();

    // ui controls
    private Button redeem;
    private EditText editTextBank;
    private EditText editTextAccount;
    private EditText editTextConfirmAccount;
    private Toolbar toolbar;

    private Bank bank;
    private Cheque cheque;
    private Senz redeemSenz;

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            ActivityUtil.hideSoftKeyboard(this);
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(RedeemActivity.this, "Successfully redeemed the iGift", Toast.LENGTH_LONG).show();
                RedeemActivity.this.finish();

                // update cheque status and account
                ChequeSource.updateChequeState(this, cheque.getUid(), ChequeState.DEPOSIT);
                ChequeSource.updateChequeAccount(this, cheque.getUid(), cheque.getAccount());
            } else if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                displayInformationMessageDialog("ERROR", "Failed to redeem iGift");
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

        redeem = (Button) findViewById(R.id.done);
        redeem.setTypeface(typeface, Typeface.BOLD);
        redeem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(RedeemActivity.this);
                onClickRedeem();
            }
        });
    }

    private void initPrefs() {
        this.bank = getIntent().getParcelableExtra("ACCOUNT_BANK");
        this.cheque = getIntent().getParcelableExtra("CHEQUE");

        editTextBank.setText(this.bank.getBankName());
    }

    private void onClickRedeem() {
        // crate account
        final String accountNo = editTextAccount.getText().toString().trim();
        final String confirmAccountNo = editTextConfirmAccount.getText().toString().trim();
        try {
            ActivityUtil.isValidRedeem(bank.getBankCode(), accountNo, confirmAccountNo);
            if (NetworkUtil.isAvailableNetwork(RedeemActivity.this)) {
                cheque.setAccount(accountNo);
                askPassword();
            } else {
                Toast.makeText(RedeemActivity.this, "No network connection", Toast.LENGTH_LONG).show();
            }
        } catch (InvalidAccountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Fail to verify account");
        } catch (InvalidInputFieldsException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Fail to verify account");
        } catch (MisMatchFieldException e) {
            displayInformationMessageDialog("ERROR", "Account number mismatch");
            e.printStackTrace();
        }
    }

    public void askPassword() {
        final Dialog dialog = new Dialog(this);

        // set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.input_password_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // texts
        TextView title = (TextView) dialog.findViewById(R.id.title);
        final EditText password = (EditText) dialog.findViewById(R.id.password);
        title.setTypeface(typeface, Typeface.BOLD);
        password.setTypeface(typeface, Typeface.NORMAL);

        // set ok button
        Button done = (Button) dialog.findViewById(R.id.done);
        done.setTypeface(typeface, Typeface.BOLD);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().trim().equalsIgnoreCase(PreferenceUtil.getAccount(RedeemActivity.this).getPassword())) {
                    dialog.cancel();
                    ActivityUtil.hideSoftKeyboard(RedeemActivity.this);
                    redeem();
                } else {
                    Toast.makeText(RedeemActivity.this, "Invalid password", Toast.LENGTH_LONG).show();
                }
            }
        });

        // cancel button
        Button cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setTypeface(typeface, Typeface.BOLD);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void redeem() {
        try {
            if (redeemSenz == null)
                redeemSenz = SenzUtil.redeemSenz(RedeemActivity.this, cheque, bank, cheque.getAccount());

            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(redeemSenz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(RedeemActivity.this, "Please wait...");
            SenzPublisher task = new SenzPublisher(RedeemActivity.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinish(String senz) {
        ActivityUtil.cancelProgressDialog();
        ActivityUtil.hideSoftKeyboard(this);
        if (senz == null) {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("ERROR", "Failed to redeem iGift");
        } else {
            // update cheque status and account
            ChequeSource.updateChequeState(this, cheque.getUid(), ChequeState.DEPOSIT);
            ChequeSource.updateChequeAccount(this, cheque.getUid(), cheque.getAccount());

            Toast.makeText(RedeemActivity.this, "Successfully redeemed the iGift", Toast.LENGTH_LONG).show();
            RedeemActivity.this.finish();
        }
    }
}
