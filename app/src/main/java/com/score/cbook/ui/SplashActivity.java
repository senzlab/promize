package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.util.PreferenceUtil;


public class SplashActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);
        //initService();
        initUi();
        initNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from service
        if (isServiceBound) {
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    private void initUi() {
        ((TextView) findViewById(R.id.splash_name)).setTypeface(typeface, Typeface.BOLD);
    }

    private void initNavigation() {
        if (PreferenceUtil.getAccount(this).getUsername().isEmpty()) {
            // no registered account yet
            // stay three seconds go to registration
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToRegistration();
                }
            }, 3000);
        } else {
            // have account,
            if (PreferenceUtil.get(this, PreferenceUtil.QUESTION1).isEmpty()) {
                navigateToQuestionInfo();
            } else {
                navigateToHome();
            }
        }
    }

    public void navigateToRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }

    public void navigateToQuestionInfo() {
        Intent intent = new Intent(this, RegistrationQuestionInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }

    public void navigateToHome() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }

}