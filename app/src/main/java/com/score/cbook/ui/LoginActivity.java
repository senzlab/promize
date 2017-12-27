package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.exceptions.PasswordMisMatchException;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.PreferenceUtils;

/**
 * Activity class that handles login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getName();

    // UI fields
    private Typeface typeface;
    private EditText editTextAccount;
    private EditText editTextPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        initUi();
        initCredentials();
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");

        editTextAccount = (EditText) findViewById(R.id.login_account_no);
        editTextPassword = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_btn);
        loginButton.setOnClickListener(this);

        editTextAccount.setTypeface(typeface, Typeface.BOLD);
        editTextPassword.setTypeface(typeface, Typeface.BOLD);
        loginButton.setTypeface(typeface, Typeface.BOLD);
    }

    private void initCredentials() {
        try {
            // saved credentials
            String acc = PreferenceUtils.getUser(this).getUsername();
            editTextAccount.setText(acc);
        } catch (NoUserException e) {
            e.printStackTrace();
        }
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
        ActivityUtils.hideSoftKeyboard(this);

        try {
            // saved credentials
            String acc = PreferenceUtils.getUser(this).getUsername();
            String pwd = PreferenceUtils.getPassword(this);

            // given credentials
            String password = editTextPassword.getText().toString().trim();
            ActivityUtils.isValidLoginFields(acc, password, acc, pwd);
            navigateToHome();
            Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show();
        } catch (NoUserException e) {
            displayInformationMessageDialog("Error", "You have to register first");
        } catch (PasswordMisMatchException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Invalid password");
        } catch (InvalidInputFieldsException e) {
            e.printStackTrace();
            displayInformationMessageDialog("Error", "Empty password");
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

