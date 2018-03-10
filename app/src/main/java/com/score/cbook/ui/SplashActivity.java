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

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);
        initService();
        initUi();
        initNavigation();
    }

    private void initUi() {
        ((TextView) findViewById(R.id.splash_name)).setTypeface(typefaceThin, Typeface.BOLD);
    }

    private void initService() {
        Intent serviceIntent = new Intent(this, SenzService.class);
        startService(serviceIntent);
    }

    private void initNavigation() {
        // determine where to go
        // start service
        try {
            PreferenceUtil.getUser(this);

            if (PreferenceUtil.getAccount(this).getAccountNo() == null) {
                // no registered account yet, go to bank select
                navigateToSplash();
            } else {
                // have user and account, so go to home
                navigateToHome();
            }
        } catch (NoUserException e) {
            // no user, go to bank select
            e.printStackTrace();
            navigateToSplash();
        }
    }

    private void navigateToSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToBankSelect();
            }
        }, 3000);
    }

    public void navigateToHome() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }

    public void navigateToBankSelect() {
        Intent intent = new Intent(this, BankTypeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }
}