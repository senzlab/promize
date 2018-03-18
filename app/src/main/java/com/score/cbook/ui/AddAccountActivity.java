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

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class AddAccountActivity extends BaseActivity {

    private EditText account;
    private EditText confirmAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_account_acctivity);

        initUi();
        initToolbar();
        initActionBar();
    }

    private void initUi() {
        account = (EditText) findViewById(R.id.account);
        confirmAccount = (EditText) findViewById(R.id.confirm_account);
        account.setTypeface(typeface, Typeface.BOLD);
        confirmAccount.setTypeface(typeface, Typeface.BOLD);

        Button yes = (Button) findViewById(R.id.register_btn);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToVishwaConfirm();
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
        intent.putExtra("ACCOUNT", account.getText().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

}

