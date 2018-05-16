package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import com.score.cbook.async.SenzPublisher;
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.InvalidPhoneNumberException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.exceptions.MisMatchPhoneNumberException;
import com.score.cbook.interfaces.ISenzPublisherListener;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;

public class RegistrationActivity extends BaseActivity implements ISenzPublisherListener {

    private static final String TAG = RegistrationActivity.class.getName();

    // ui controls
    private Button registerBtn;
    private EditText editTextPhone;
    private EditText editTextConfirmPhone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private String zaddress;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Register");

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

    private void initUi() {
        editTextPhone = (EditText) findViewById(R.id.registering_user_id);
        editTextConfirmPhone = (EditText) findViewById(R.id.registering_confirm_user_id);
        editTextPassword = (EditText) findViewById(R.id.registering_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.registering_confirm_password);


        editTextPhone.setTypeface(typeface, Typeface.NORMAL);
        editTextConfirmPhone.setTypeface(typeface, Typeface.NORMAL);
        editTextPassword.setTypeface(typeface, Typeface.NORMAL);
        editTextConfirmPassword.setTypeface(typeface, Typeface.NORMAL);

        registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setTypeface(typeface, Typeface.BOLD);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickRegister();
            }
        });
    }

    private void onClickRegister() {
        ActivityUtil.hideSoftKeyboard(this);

        // crate account
        final String phone = editTextPhone.getText().toString().trim();
        final String confirmPhone = editTextConfirmPhone.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtil.isValidRegistrationFields(phone, confirmPhone, password, confirmPassword);
            String confirmationMessage = "<font color=#636363>Please confirm to register as </font> <font color=#F37920>" + "<b>" + phone + "</b>" + "</font> <font color=#636363> in iGifts </font> ";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(RegistrationActivity.this)) {
                        account = new Account();
                        account.setUsername(phone);
                        account.setPassword(password);
                        if (PreferenceUtil.get(RegistrationActivity.this, PreferenceUtil.Z_ADDRESS).isEmpty())
                            doReg(phone);
                    } else {
                        Toast.makeText(RegistrationActivity.this, "No network connection", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Invalid password. Password should contains more than 7 characters with special character");
        } catch (MisMatchFieldException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Mismatching password and confirm password");
        } catch (InvalidPhoneNumberException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Invalid phone no. Phone no should contains 10 digits and start with 0");
        } catch (MisMatchPhoneNumberException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Mismatching Phone no and confirm Phone no");
        }
    }

    private void doReg(String phone) {
        try {
            // generate keypair
            // generate senzie address
            CryptoUtil.initKeys(this);
            zaddress = PhoneBookUtil.getFormattedPhoneNo(this, phone);

            // create senz
            Senz senz = SenzUtil.regSenz(this, zaddress);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(this, "Please wait...");
            SenzPublisher task = new SenzPublisher(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToQuestionInfo() {
        Intent intent = new Intent(RegistrationActivity.this, RegistrationQuestionInfoActivity.class);
        RegistrationActivity.this.startActivity(intent);
        RegistrationActivity.this.finish();
    }

    @Override
    public void onFinish(String senz) {
        ActivityUtil.cancelProgressDialog();
        if (senz == null) {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("ERROR", "Registration fail");
        } else {
            // OK
            Toast.makeText(this, "Registration done", Toast.LENGTH_LONG).show();

            PreferenceUtil.put(this, PreferenceUtil.Z_ADDRESS, zaddress);
            PreferenceUtil.put(this, PreferenceUtil.USERNAME, account.getUsername());
            PreferenceUtil.put(this, PreferenceUtil.PASSWORD, account.getPassword());
            navigateToQuestionInfo();
        }
    }
}
