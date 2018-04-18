package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    // UI fields
    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button loginButton;

    // account
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        initUi();
        initAccount();
        initToolbar();
        initActionBar();
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Login");

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


    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        editTextAccount = (EditText) findViewById(R.id.login_account_no);
        editTextPassword = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_btn);
        loginButton.setOnClickListener(this);

        editTextAccount.setTypeface(typeface, Typeface.NORMAL);
        editTextPassword.setTypeface(typeface, Typeface.NORMAL);
        loginButton.setTypeface(typeface, Typeface.BOLD);
    }

    private void initAccount() {
        account = PreferenceUtil.getAccount(this);
        if (!account.getPhoneNo().isEmpty())
            editTextAccount.setText(account.getPhoneNo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v == loginButton) {
            onClickLogin();
        }
    }

    /**
     * login button action,
     */
    private void onClickLogin() {
        ActivityUtil.hideSoftKeyboard(this);

        try {
            // saved credentials
            Account account = PreferenceUtil.getAccount(this);

            // given credentials
            String accountNo = editTextAccount.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            ActivityUtil.isValidLoginFields(accountNo, password, account.getAccountNo(), account.getPassword());
            navigateToHome();
            Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show();
        } catch (MisMatchFieldException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Invalid password");
        } catch (InvalidInputFieldsException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Empty password");
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        startActivity(intent);
        this.finish();
    }

}

