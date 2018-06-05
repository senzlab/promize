package com.score.cbook.ui;

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
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.exceptions.SamePasswordException;
import com.score.cbook.pojo.Account;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.PreferenceUtil;

public class PasswordChangeActivity extends BaseActivity {

    private static final String TAG = PasswordChangeActivity.class.getName();

    private EditText current_password;
    private EditText new_password;
    private EditText new_confirm_password;

    private Button update_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

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

    private void initUi() {

        // text views
        current_password = (EditText) findViewById(R.id.current_password);
        new_password = (EditText) findViewById(R.id.new_password);
        new_confirm_password = (EditText) findViewById(R.id.new_confirm_password);
        current_password.setTypeface(typeface, Typeface.NORMAL);
        new_password.setTypeface(typeface, Typeface.NORMAL);
        new_confirm_password.setTypeface(typeface, Typeface.NORMAL);

        // buttons
        update_btn = (Button) findViewById(R.id.update_btn);
        update_btn.setTypeface(typeface, Typeface.BOLD);
        final Account useAccount = PreferenceUtil.getAccount(this);

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = current_password.getText().toString().trim();
                String newPassword = new_password.getText().toString().trim();
                String newConfirmPassword = new_confirm_password.getText().toString().trim();

                if (useAccount.getPassword().equals(currentPassword)) {
                    try {
                        ActivityUtil.isValidChangePassword(currentPassword, newPassword, newConfirmPassword);
                        PreferenceUtil.put(PasswordChangeActivity.this, PreferenceUtil.PASSWORD, newPassword);
                        Toast.makeText(PasswordChangeActivity.this, "Successfully changed password", Toast.LENGTH_LONG).show();
                        finish();
                    } catch (InvalidPasswordException e) {
                        e.printStackTrace();
                        displayInformationMessageDialog("ERROR", "Invalid password. Password should contains more than 7 characters with special character");
                    } catch (MisMatchFieldException e) {
                        e.printStackTrace();
                        displayInformationMessageDialog("ERROR", "Mismatching password and confirm password");
                    } catch (SamePasswordException e) {
                        e.printStackTrace();
                        displayInformationMessageDialog("ERROR", "Your old password and new password are same, please choose a different new password");
                    }
                } else {
                    displayInformationMessageDialog("ERROR", "Invalid current password ");
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
        titleText.setText("Change Password");

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

}
