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
import com.score.cbook.async.SenzPublisher;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.interfaces.ISenzPublisherListener;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.PrivateKey;


public class PromizePreviewActivity extends BaseActivity implements ISenzPublisherListener {

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

        this.cheque = getIntent().getParcelableExtra("CHEQUE");
        initUi();

        // update viewed state
        if (cheque.isViewed()) {
            loadBitmap(imageView, cheque.getUid());
        } else {
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
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(this, "Fetching iGift");
            SenzPublisher task = new SenzPublisher(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onFinish(String senz) {
        ActivityUtil.cancelProgressDialog();
        if (senz == null) {
            Toast.makeText(this, "Failed to fetch iGift", Toast.LENGTH_LONG).show();
        } else {
            // parse senz and get blob
            Senz bSenz = SenzParser.parse(senz);
            if (bSenz.getAttributes().containsKey("blob")) {
                String imgName = bSenz.getAttributes().get("uid") + ".jpg";
                ImageUtil.saveImg(imgName, bSenz.getAttributes().get("blob"));

                cheque.setViewed(true);
                ChequeSource.markChequeViewed(this, cheque.getUid());
                loadBitmap(imageView, cheque.getUid());
            } else {
                Toast.makeText(this, "Failed to fetch iGift", Toast.LENGTH_LONG).show();
            }
        }
    }
}
