package com.score.cbook.ui;

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
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;

public class PasswordResetActivity extends BaseActivity {

    // UI fields
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_reset_activity);

        initUi();
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
        titleText.setText("Reset password");

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
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirm_password);

        editTextPassword.setTypeface(typeface, Typeface.NORMAL);
        editTextConfirmPassword.setTypeface(typeface, Typeface.NORMAL);

        loginButton = (Button) findViewById(R.id.login_btn);
        loginButton.setTypeface(typeface, Typeface.BOLD);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickReset();
            }
        });
    }

    private void onClickReset() {
        ActivityUtil.hideSoftKeyboard(this);
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        try {
            ActivityUtil.isValidPasswordFields(password, password, confirmPassword);
            PreferenceUtil.updatePassword(this, password);
            finish();
            Toast.makeText(this, "Successfully reset password", Toast.LENGTH_LONG).show();
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Invalid password. Password should contains more than 7 characters with special character");
        } catch (MisMatchFieldException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Mismatching password and confirm password");
        }
    }

}

