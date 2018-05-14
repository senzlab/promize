package com.score.cbook.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.async.FetchTask;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.interfaces.IFetchTaskListener;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.PrivateKey;

public class PromizePreviewActivity extends BaseActivity implements IFetchTaskListener {

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
            // fetch blob from zwitch
            fetchBlob(cheque.getUid());
        }
    }

    private void initUi() {
        imageView = (ImageView) findViewById(R.id.cheque_preview);
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
                Intent intent = new Intent(PromizePreviewActivity.this, BankListActivity.class);
                intent.putExtra("CHEQUE", cheque);
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

    private void fetchBlob(String uid) {
        // create senz
        try {
            Senz senz = SenzUtil.blobSenz(this, uid);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);

            // senz msg
            String message = SenzParser.senzMsg(senzPayload, signature);
            SenzMsg senzMsg = new SenzMsg(uid, message);

            FetchTask task = new FetchTask(this, FetchTask.BLOB_API);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, senzMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinishTask(Integer status) {
        ActivityUtil.cancelProgressDialog();
        if (status == 200) {
            cheque.setViewed(true);
            ChequeSource.markChequeViewed(this, cheque.getUid());
            loadBitmap(imageView, cheque.getUid());
        } else {
            Toast.makeText(this, "Failed to fetch iGift", Toast.LENGTH_LONG).show();
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
