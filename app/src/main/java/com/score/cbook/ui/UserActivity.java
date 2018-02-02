package com.score.cbook.ui;


import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UserActivity extends BaseActivity {

    private FloatingActionButton cancel;

    private TextView name;
    private TextView nameV;
    private TextView phone;
    private TextView phoneV;
    private TextView account;
    private TextView accountV;
    private TextView cheque;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_p_layout);

        initUi();
        initPrefs();
    }

    private void initUi() {
        // text views
        name = (TextView) findViewById(R.id.name);
        nameV = (TextView) findViewById(R.id.namev);
        phone = (TextView) findViewById(R.id.phone);
        phoneV = (TextView) findViewById(R.id.phonev);
        account = (TextView) findViewById(R.id.account);
        accountV = (TextView) findViewById(R.id.accountv);
        cheque = (TextView) findViewById(R.id.cheque);
        message = (TextView) findViewById(R.id.message);

        // font
        name.setTypeface(typeface, Typeface.NORMAL);
        nameV.setTypeface(typeface, Typeface.NORMAL);
        phone.setTypeface(typeface, Typeface.NORMAL);
        phoneV.setTypeface(typeface, Typeface.NORMAL);
        account.setTypeface(typeface, Typeface.NORMAL);
        accountV.setTypeface(typeface, Typeface.NORMAL);
        cheque.setTypeface(typeface, Typeface.BOLD);
        message.setTypeface(typeface, Typeface.BOLD);

        cancel = (FloatingActionButton) findViewById(R.id.close);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initPrefs() {
    }

    private void loadBitmap(ImageView view, String uid) {
        // load image via picasso
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/ChequeBook/" + uid + ".jpg");
        Picasso.with(this)
                .load(file)
                .error(R.drawable.rahaslogo_3)
                .into(view);
    }

}
