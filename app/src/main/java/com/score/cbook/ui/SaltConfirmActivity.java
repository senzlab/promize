package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.async.PostTask;
import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SaltConfirmActivity extends BaseActivity implements IPostTaskListener {

    private static final String TAG = SaltConfirmActivity.class.getName();

    private EditText amount;
    private int retry = 0;

    private void handleSenz(Senz senz) {
        if (senz.getAttributes().containsKey("status")) {
            String msg = senz.getAttributes().get("status");
            if (msg != null && msg.equalsIgnoreCase("SUCCESS")) {
                ActivityUtil.cancelProgressDialog();

                // set account state as verified
                PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_STATE, "VERIFIED");
                Toast.makeText(this, "Your account has been verified", Toast.LENGTH_LONG).show();
                SaltConfirmActivity.this.finish();
            } else if (msg != null && msg.equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                displayInformationMessageDialog("ERROR", "Fail to verify account");
            } else if (msg != null && msg.equalsIgnoreCase("VERIFICATION_FAIL")) {
                ActivityUtil.cancelProgressDialog();
                String informationMessage = "Verification fail. Please contact sampath support regarding this issue";
                displayInformationMessageDialog("ERROR", informationMessage);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salt_confirm_activity);

        initUi();
        initToolbar();
        initActionBar();
    }

    private void initUi() {
        amount = (EditText) findViewById(R.id.amount);
        amount.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.register_btn);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retry >= 3) {
                    displayInformationMessageDialog("ERROR", "Maximum no of retry attempts exceeded");
                } else {
                    ActivityUtil.showProgressDialog(SaltConfirmActivity.this, "Please wait...");
                    confirmSalt();
                }
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
        titleText.setText("Confirm account");

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

    private void confirmSalt() {
        try {
            String salt = amount.getText().toString().trim();
            Senz senz = SenzUtil.saltSenz(this, salt);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);

            // senz msg
            String uid = senz.getAttributes().get("uid");
            String message = SenzParser.senzMsg(senzPayload, signature);
            SenzMsg senzMsg = new SenzMsg(uid, message);

            PostTask task = new PostTask(this, PostTask.UZER_API, senzMsg);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "PUT");
            retry++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinishTask(Integer status) {
        if (status == 200) {
            PreferenceUtil.put(this, PreferenceUtil.ACCOUNT_STATE, "VERIFIED");
            Toast.makeText(this, "Your account has been verified", Toast.LENGTH_LONG).show();
            SaltConfirmActivity.this.finish();
        } else {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("ERROR", "Fail to verify account");
        }
    }
}

