package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.util.PreferenceUtil;

public class RegistrationQuestionActivity extends BaseActivity {

    // UI fields
    private TextView question1Text;
    private TextView question2Text;
    private TextView question3Text;
    private EditText question1;
    private EditText question2;
    private EditText question3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_question_activity);

        initUi();
        initToolbar();
        initActionBar();
    }

    private void initUi() {
        question1Text = (TextView) findViewById(R.id.question1_text);
        question2Text = (TextView) findViewById(R.id.question2_text);
        question3Text = (TextView) findViewById(R.id.question3_text);
        question1 = (EditText) findViewById(R.id.question1);
        question2 = (EditText) findViewById(R.id.question2);
        question3 = (EditText) findViewById(R.id.question3);

        question1Text.setTypeface(typeface, Typeface.NORMAL);
        question2Text.setTypeface(typeface, Typeface.NORMAL);
        question3Text.setTypeface(typeface, Typeface.NORMAL);
        question1.setTypeface(typeface, Typeface.NORMAL);
        question2.setTypeface(typeface, Typeface.NORMAL);
        question3.setTypeface(typeface, Typeface.NORMAL);

        Button yes = (Button) findViewById(R.id.register_btn);
        yes.setTypeface(typeface, Typeface.BOLD);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSave();
            }
        });
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.add_user_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        titleText.setText("Answer questions");

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

    private void onClickSave() {
        // validate input
        final String answer1 = question1.getText().toString().trim();
        final String answer2 = question2.getText().toString().trim();
        final String answer3 = question3.getText().toString().trim();

        if (answer1.isEmpty() || answer2.isEmpty() || answer3.isEmpty()) {
            Toast.makeText(this, "Invalid answers", Toast.LENGTH_LONG).show();
        } else {
            displayConfirmationMessageDialog("Are you sure you want to save the answers for the questions", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // save answers
                    PreferenceUtil.put(RegistrationQuestionActivity.this, PreferenceUtil.QUESTION1, answer1);
                    PreferenceUtil.put(RegistrationQuestionActivity.this, PreferenceUtil.QUESTION2, answer2);
                    PreferenceUtil.put(RegistrationQuestionActivity.this, PreferenceUtil.QUESTION3, answer3);
                    navigateToHome();
                    Toast.makeText(RegistrationQuestionActivity.this, "Successfully saved answers", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        this.startActivity(intent);
        this.finish();
    }

}

