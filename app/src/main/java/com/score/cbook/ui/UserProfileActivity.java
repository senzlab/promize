package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.ImageUtils;
import com.score.cbook.utils.NetworkUtil;
import com.score.cbook.utils.PhoneBookUtil;
import com.score.cbook.utils.SenzUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = UserProfileActivity.class.getName();

    private ImageView backImageView;
    private ImageView userImageView;

    private TextView phone;
    private TextView phoneV;
    private TextView account;
    private TextView accountV;
    private Button writeCheque;
    private Button writeMessage;
    private NestedScrollView scrollView;

    private ChequeUser chequeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initUser();
        initUi();
        initToolbar();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putParcelable("SECRET_USER", chequeUser);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            chequeUser = savedInstanceState.getParcelable("SECRET_USER");
        }
    }

    private void initUser() {
        if (getIntent().getExtras() != null)
            chequeUser = getIntent().getExtras().getParcelable("SECRET_USER");
    }

    private void initUi() {
        // text views
        phone = (TextView) findViewById(R.id.phone);
        phoneV = (TextView) findViewById(R.id.phonev);
        account = (TextView) findViewById(R.id.account);
        accountV = (TextView) findViewById(R.id.accountv);
        phone.setTypeface(typeface, Typeface.NORMAL);
        phoneV.setTypeface(typeface, Typeface.NORMAL);
        account.setTypeface(typeface, Typeface.NORMAL);
        accountV.setTypeface(typeface, Typeface.NORMAL);

        userImageView = (ImageView) findViewById(R.id.clickable_image);

        // user values
        phoneV.setText(chequeUser.getPhone());
        accountV.setText(chequeUser.getUsername());
        if (chequeUser.getImage() != null)
            userImageView.setImageBitmap(ImageUtils.decodeBitmap(chequeUser.getImage()));

        // buttons
        writeCheque = (Button) findViewById(R.id.write_cheque);
        writeMessage = (Button) findViewById(R.id.writer_message);
        writeCheque.setTypeface(typeface, Typeface.BOLD);
        writeMessage.setTypeface(typeface, Typeface.BOLD);
        writeCheque.setOnClickListener(this);
        writeMessage.setOnClickListener(this);

        // scroller view
        //scrollView = (NestedScrollView) findViewById(R.id.nested_scroll);
        //scrollView.scrollTo(0, 200);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        View header = getLayoutInflater().inflate(R.layout.profile_header, null);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.addView(header);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(PhoneBookUtil.getContactName(this, chequeUser.getPhone()));
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.colorPrimary));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.colorPrimary));

        backImageView = (ImageView) findViewById(R.id.back_btn);
        backImageView.setOnClickListener(this);
    }

    private void getProfilePhoto() {
        if (NetworkUtil.isAvailableNetwork(UserProfileActivity.this)) {
            if (isServiceBound) {
                // create senz attributes
                HashMap<String, String> senzAttributes = new HashMap<>();
                String timestamp = ((Long) (System.currentTimeMillis() / 1000)).toString();
                senzAttributes.put("time", timestamp);
                senzAttributes.put("cam", "");
                senzAttributes.put("uid", SenzUtils.getUid(this, timestamp));

                // new senz
                String id = "_ID";
                String signature = "_SIGNATURE";
                SenzTypeEnum senzType = SenzTypeEnum.GET;
                Senz senz = new Senz(id, signature, senzType, null, new User(chequeUser.getId(), chequeUser.getUsername()), senzAttributes);

                ActivityUtils.showProgressDialog(this, "Calling selfie...");
                send(senz);
            } else {
                Toast.makeText(this, "Cannot connect with service", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == backImageView) {
            finish();
        }
    }

}
