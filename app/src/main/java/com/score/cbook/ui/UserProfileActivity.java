package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.PhoneBookUtil;
import com.squareup.picasso.Picasso;

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
    private Button sendGift;

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

        // contact image
        Picasso.with(this)
                .load(PhoneBookUtil.getContactUri(this, chequeUser.getPhone()))
                .placeholder(R.drawable.default_user_icon)
                .centerInside()
                .error(R.drawable.df_user)
                .into(userImageView);

        // buttons
        writeCheque = (Button) findViewById(R.id.write_cheque);
        writeMessage = (Button) findViewById(R.id.writer_message);
        sendGift = (Button) findViewById(R.id.send_gift);
        writeCheque.setTypeface(typeface, Typeface.BOLD);
        writeMessage.setTypeface(typeface, Typeface.BOLD);
        sendGift.setTypeface(typeface, Typeface.BOLD);
        writeCheque.setOnClickListener(this);
        writeMessage.setOnClickListener(this);
        sendGift.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v == backImageView) {
            finish();
        } else if (v == sendGift) {
            Intent intent = new Intent(this, NewGiftActivity.class);
            intent.putExtra("USER", chequeUser);
            startActivity(intent);
        }
    }

}
