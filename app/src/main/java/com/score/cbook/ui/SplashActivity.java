package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.remote.SenzService;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

/**
 * Splash activity, send login query from here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SplashActivity extends BaseActivity {

    // senzie
    private User senzie;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                handleSenz(senz);
            }
        }
    };

    private void handleSenz(Senz senz) {
        if (senz.getAttributes().containsKey("status")) {
            // received reg status
            String msg = senz.getAttributes().get("status");
            if (msg != null && (msg.equalsIgnoreCase("REG_DONE"))) {
                // reg success
                // save user
                PreferenceUtil.saveSenzeisAddress(this, senzie);
            } else if (msg != null && msg.equalsIgnoreCase("REG_ALR")) {

            } else if (msg != null && msg.equalsIgnoreCase("REG_FAIL")) {
                Toast.makeText(this, "Service unavailable", Toast.LENGTH_LONG).show();
            }
        } else if (senz.getAttributes().containsKey("pubkey")) {
            // received auth key
            // navigate to bank select
            navigateToRegistration();
        }
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);
        initService();
        initUi();
        initNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindToService();
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
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
        try {
            PreferenceUtil.getSenzieAddress(this);

            if (PreferenceUtil.getAccount(this).getPassword().isEmpty()) {
                // no registered account yet
                // stay three seconds go to bank select
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // go to bank select
                        navigateToRegistration();
                    }
                }, 3000);
            } else {
                // have user and account, so go to home
                navigateToLogin();
            }
        } catch (NoUserException e) {
            // stay to seconds and init senzie
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // generate keys and reg
                    initSenzie();
                }
            }, 2000);
        }
    }

    private void initSenzie() {
        try {
            // generate keypair
            // generate senzie address
            CryptoUtil.initKeys(this);
            String senzieAddress = CryptoUtil.getSenzieAddress(this);

            // send reg
            send(SenzUtil.regSenz(this, senzieAddress));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }

    public void navigateToRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashActivity.this.finish();
    }
}