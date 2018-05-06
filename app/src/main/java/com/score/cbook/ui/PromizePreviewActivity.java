package com.score.cbook.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.score.cbook.R;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.pojo.Bank;
import com.score.cbook.pojo.Cheque;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PromizePreviewActivity extends BaseActivity {

    private FloatingActionButton cancel;
    private FloatingActionButton done;
    private ImageView imageView;

    private Cheque cheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.cheque_p);

        initPrefs();
        initUi();
    }

    private void initPrefs() {
        this.cheque = getIntent().getParcelableExtra("CHEQUE");

        // update viewed state
        if (!cheque.isViewed()) {
            cheque.setViewed(true);
            ChequeSource.markChequeViewed(this, cheque.getUid());
        }
    }

    private void initUi() {
        imageView = (ImageView) findViewById(R.id.cheque_preview);
        loadBitmap(imageView, cheque.getUid());

        cancel = (FloatingActionButton) findViewById(R.id.close);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done = (FloatingActionButton) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PromizePreviewActivity.this, RedeemActivity.class);
                intent.putExtra("CHEQUE", cheque);
                intent.putExtra("ACCOUNT_BANK", new Bank("7179", "Sampath bank"));
                startActivity(intent);
                PromizePreviewActivity.this.finish();
            }
        });

        if (cheque.isMyCheque()) {
            done.setVisibility(View.GONE);
        } else {
            if (cheque.getChequeState() == ChequeState.DEPOSIT) {
                done.setVisibility(View.GONE);
            } else {
                done.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadBitmap(ImageView view, String uid) {
        // load image via picasso
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/iGift/" + uid + ".jpg");
        Picasso.with(this)
                .load(file)
                .error(R.drawable.rahaslogo_3)
                .into(view);
    }

}
