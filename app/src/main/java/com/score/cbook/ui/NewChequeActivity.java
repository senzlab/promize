package com.score.cbook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.async.CheckImageGenerator;
import com.score.cbook.interfaces.ICheckImageGeneratorListener;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.PhoneBookUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewChequeActivity extends BaseActivity implements ICheckImageGeneratorListener, DatePickerDialog.OnDateSetListener {

    private static final String TAG = NewChequeActivity.class.getName();

    // ui controls
    private EditText userEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private Button sendButton;

    private ChequeUser chequeUser;
    private Cheque cheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_cheque_activity_layout);

        initPrefs();
        initUi();
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

    private void initPrefs() {
        this.chequeUser = getIntent().getParcelableExtra("USER");
    }

    private void initUi() {
        userEditText = (EditText) findViewById(R.id.new_cheque_username);
        amountEditText = (EditText) findViewById(R.id.new_cheque_amount);
        dateEditText = (EditText) findViewById(R.id.new_cheque_date);

        userEditText.setTypeface(typeface, Typeface.BOLD);
        amountEditText.setTypeface(typeface, Typeface.BOLD);
        dateEditText.setTypeface(typeface, Typeface.BOLD);

        userEditText.setText(PhoneBookUtil.getContactName(this, chequeUser.getPhone()));

        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // show date picker
                    onFocusDate();
                }
            }
        });
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFocusDate();
            }
        });

        sendButton = (Button) findViewById(R.id.new_cheque_send);
        sendButton.setTypeface(typeface, Typeface.BOLD);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.hideSoftKeyboard(NewChequeActivity.this);
                onClickPreview();
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.new_cheque_header, null));
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

    private void onFocusDate() {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date");
    }

    private void onClickPreview() {
        String amount = amountEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        if (amount.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Empty fields", Toast.LENGTH_LONG).show();
        } else {
            ActivityUtils.showProgressDialog(this, "Generating cheque...");

            // create cheque
            cheque = new Cheque();
            cheque.setUser(chequeUser);
            cheque.setAmount(Integer.parseInt(amount));
            cheque.setDate(date);
            cheque.setMyCheque(false);

            // create image via async task
            CheckImageGenerator imageCreator = new CheckImageGenerator(this, this);
            imageCreator.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cheque);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String selectedDate = sdf.format(cal.getTime());
        dateEditText.setText(selectedDate);
    }

    @Override
    public void onGenerate(String chequeImg) {
        ActivityUtils.cancelProgressDialog();

        cheque.setBlob(chequeImg);

        // cheque preview
        Intent intent = new Intent(this, ChequePreviewActivity.class);
        intent.putExtra("CHEQUE", cheque);
        startActivity(intent);
        NewChequeActivity.this.finish();
    }
}
