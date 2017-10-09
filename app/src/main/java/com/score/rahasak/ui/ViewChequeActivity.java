package com.score.rahasak.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.utils.ActivityUtils;

public class ViewChequeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ViewChequeActivity.class.getName();

    // ui controls
    private EditText userEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private Button send;

    private Cheque cheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_cheque_activity_layout);

        initUi();
        initCheque();
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
        userEditText = (EditText) findViewById(R.id.view_cheque_username);
        amountEditText = (EditText) findViewById(R.id.view_cheque_amount);
        dateEditText = (EditText) findViewById(R.id.view_cheque_date);

        userEditText.setTypeface(typeface, Typeface.BOLD);
        amountEditText.setTypeface(typeface, Typeface.BOLD);
        dateEditText.setTypeface(typeface, Typeface.BOLD);

        send = (Button) findViewById(R.id.view_cheque_send);
        send.setOnClickListener(this);
    }

    private void initCheque() {
        cheque = getIntent().getParcelableExtra("CHEQUE");

        userEditText.setText(cheque.getUser().getUsername());
        amountEditText.setText(Integer.toString(cheque.getAmount()));
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.view_cheque_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);

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

    private void onClickPreview() {
        // cheque preview
        Intent intent = new Intent(this, ChequePActivity.class);
        intent.putExtra("UID", cheque.getUid());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == send) {
            ActivityUtils.hideSoftKeyboard(this);
            onClickPreview();
        }
    }
}
