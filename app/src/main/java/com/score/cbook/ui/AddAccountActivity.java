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

import com.score.cbook.R;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.util.ActivityUtil;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class AddAccountActivity extends BaseActivity {

    private EditText accountText;
    private EditText confirmAccountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_account_acctivity);

        initUi();
        initToolbar();
        initActionBar();
    }

    private void initUi() {
        accountText = (EditText) findViewById(R.id.account);
        confirmAccountText = (EditText) findViewById(R.id.confirm_account);
        accountText.setTypeface(typeface, Typeface.NORMAL);
        confirmAccountText.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.register_btn);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountText.getText().toString().trim();
                String confirmAccount = confirmAccountText.getText().toString().trim();
                try {
                    ActivityUtil.isValidAccount(account, confirmAccount);
                    navigateToVishwaConfirm();
                } catch (InvalidInputFieldsException e) {
                    e.printStackTrace();
                    displayInformationMessageDialog("ERROR", "Account number should be 12 character length and start with 1");
                } catch (MisMatchFieldException e) {
                    e.printStackTrace();
                    displayInformationMessageDialog("ERROR", "Mismatching account number");
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
        titleText.setText("Add account");

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

    private void navigateToVishwaConfirm() {
        Intent intent = new Intent(AddAccountActivity.this, AccountVerifyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("ACCOUNT", accountText.getText().toString().trim());
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

}

