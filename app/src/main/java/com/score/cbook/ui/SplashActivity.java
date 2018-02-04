package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.remote.SenzService;
import com.score.cbook.util.PreferenceUtil;

/**
 * Splash activity, send login query from here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SplashActivity extends BaseActivity {
    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private static final String TAG = SplashActivity.class.getName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);
        startService();
        initNavigation();
        setupSplashText();
    }

    private void setupSplashText() {
        ((TextView) findViewById(R.id.splash_name)).setTypeface(typefaceThin, Typeface.BOLD);
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, SenzService.class);
        startService(serviceIntent);
    }

    /**
     * Determine where to go from here
     */
    private void initNavigation() {
        // determine where to go
        // start service
        try {
            PreferenceUtil.getUser(this);

            // have user, so move to home
            navigateToHome();
        } catch (NoUserException e) {
            e.printStackTrace();
            navigateToSplash();
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateRegistration();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void navigateRegistration() {
        // no user, so move to registration
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    public void navigateToHome() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();

    }
}