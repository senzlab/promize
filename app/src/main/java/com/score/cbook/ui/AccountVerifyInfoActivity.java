package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.async.ContractExecutor;
import com.score.cbook.interfaces.IContractExecutorListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;
import java.util.List;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class AccountVerifyInfoActivity extends BaseActivity implements IContractExecutorListener {

    // UI fields
    private TextView hi;
    private TextView message;

    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acc_verify_info_activity);

        initUi();
        initToolbar();
        initActionBar();
        initPrefs();
    }

    private void initUi() {
        hi = (TextView) findViewById(R.id.hi_message);
        message = (TextView) findViewById(R.id.welcome_message);
        hi.setTypeface(typeface, Typeface.NORMAL);
        message.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.yes);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });

        Button no = (Button) findViewById(R.id.no);
        no.setTypeface(typeface, Typeface.BOLD);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // exit
                finish();
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Verify account");

        // back button
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setVisibility(View.INVISIBLE);
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

    private void initPrefs() {
        if (getIntent().getExtras() != null)
            account = getIntent().getExtras().getString("ACCOUNT");
    }

    private void verifyAccount() {
        // create senz
        try {
            Senz senz = SenzUtil.accountSenz(this, account);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(AccountVerifyInfoActivity.this, "Please wait...");
            SenzMsg senzMsg = new SenzMsg(senz.getAttributes().get("uid"), message);
            ContractExecutor task = new ContractExecutor(senzMsg, AccountVerifyInfoActivity.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToConfirm() {
        Intent intent = new Intent(AccountVerifyInfoActivity.this, SaltConfirmInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        finish();
    }

    @Override
    public void onFinishTask(List<Senz> senzes) {
        ActivityUtil.cancelProgressDialog();
        if (senzes.size() == 0) {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("Error", "Fail to verify account");
        } else {
            Senz z = senzes.get(0);
            if (z.getAttributes().get("status").equalsIgnoreCase("200")) {
                // reset account state
                // save account
                // navigate to salt confirm
                PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_STATE, "PENDING");
                PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_BANK, SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
                PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_NO, account);
                navigateToConfirm();
            } else {
                displayInformationMessageDialog("Error", "Fail to send request");
            }
        }
    }
}

