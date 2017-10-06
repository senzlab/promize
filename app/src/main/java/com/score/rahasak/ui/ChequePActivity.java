package com.score.rahasak.ui;


import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;

import com.score.rahasak.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ChequePActivity extends BaseActivity {

    private FloatingActionButton cancel;
    private FloatingActionButton done;
    private ImageView imageView;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheque_p);

        initPrefs();
        initUi();
    }

    private void initPrefs() {
        this.uid = getIntent().getStringExtra("UID");
    }

    private void initUi() {
        imageView = (ImageView) findViewById(R.id.cheque_preview);
        loadBitmap(imageView, uid);

        cancel = (FloatingActionButton) findViewById(R.id.close);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done = (FloatingActionButton) findViewById(R.id.done);
        done.setVisibility(View.GONE);
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
