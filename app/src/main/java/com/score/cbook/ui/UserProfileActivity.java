package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.PhoneBookUtil;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    private ImageView backImageView;
    private ImageView deleteImageView;
    private ImageView userImageView;
    private FloatingActionButton deleteButton;

    private TextView title;
    private TextView phone;
    private TextView phoneV;
    private TextView account;
    private TextView accountV;

    private ChequeUser chequeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initUser();
        initUi();
        initToolbar();
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
        phoneV.setTypeface(typeface, Typeface.BOLD);
        account.setTypeface(typeface, Typeface.NORMAL);
        accountV.setTypeface(typeface, Typeface.BOLD);

        userImageView = (ImageView) findViewById(R.id.clickable_image);
        deleteButton = (FloatingActionButton) findViewById(R.id.delete_user);
        deleteButton.setOnClickListener(this);

        // user values
        accountV.setText(PhoneBookUtil.getContactName(this, chequeUser.getPhone()));
        phoneV.setText(chequeUser.getPhone());

        // contact image
        Picasso.with(this)
                .load(PhoneBookUtil.getImageUri(this, chequeUser.getPhone()))
                .placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon)
                .into(userImageView);
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

        title = (TextView) header.findViewById(R.id.title);
        title.setTypeface(typeface, Typeface.BOLD);
        title.setText(PhoneBookUtil.getContactName(this, chequeUser.getPhone()));

        backImageView = (ImageView) header.findViewById(R.id.back_btn);
        backImageView.setOnClickListener(this);

        deleteImageView = (ImageView) header.findViewById(R.id.done_btn);
        deleteImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == backImageView) {
            finish();
        } else if (v == deleteButton) {
            delete();
        }
    }

    private void delete() {
        if (!ChequeSource.hasChequesToRedeem(this, chequeUser.getUsername())) {
            displayConfirmationMessageDialog("Confirm", "Are you sure your want to remove the user", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // delete from db
                    UserSource.deleteUser(UserProfileActivity.this, chequeUser.getUsername());
                    ChequeSource.deleteChequesOfUser(UserProfileActivity.this, chequeUser.getUsername());
                    SecretSource.deleteSecretsOfUser(UserProfileActivity.this, chequeUser.getUsername());

                    finish();
                }
            });
        } else {
            displayInformationMessageDialog("Error", "You have igifts from this contact which not yet redeemed. Please redeem them before removing the contact");
        }
    }

}
