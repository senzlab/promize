package com.score.rahasak.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.score.rahasak.R;
import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.enums.DeliveryState;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.pojo.Secret;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.ActivityUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.SenzUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class ChequePreviewActivity extends BaseActivity {

    private static final String TAG = ChequePActivity.class.getName();

    // ui controls
    private FloatingActionButton cancel;
    private FloatingActionButton done;
    private ImageView chqueImg;
    private Cheque cheque;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                handleSenz(senz);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheque_p);

        initPrefs();
        initCheque();
        initUi();
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
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
    }

    private void initPrefs() {
        this.cheque = getIntent().getParcelableExtra("CHEQUE");
    }

    private void initCheque() {
        chqueImg = (ImageView) findViewById(R.id.cheque_preview);

        // sign cheque
        Bitmap chq = ImageUtils.loadImg(this, "chq.jpg");
        Bitmap sig = ImageUtils.loadImg(this, "sign.png");

        // sign cheque
        // add text
        Bitmap sChq = ImageUtils.addSign(chq, sig);

        // compress
        byte[] bytes = ImageUtils.bmpToBytes(sChq);
        byte[] compBytes = ImageUtils.compressImage(bytes);

        // set cheque
        cheque.setImg(Base64.encodeToString(compBytes, Base64.DEFAULT));

        Bitmap cChq = ImageUtils.bytesToBmp(compBytes);
        chqueImg.setImageBitmap(cChq);
    }

    private void initUi() {
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
                ActivityUtils.showProgressDialog(ChequePreviewActivity.this, "Sharing...");
                Long timestamp = System.currentTimeMillis() / 1000;
                saveSecret(timestamp);
                sendCheque(timestamp);
            }
        });
    }

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(ChequePreviewActivity.this, "Share success", Toast.LENGTH_LONG).show();
                ChequePreviewActivity.this.finish();
            }
        }
    }

    private void saveSecret(Long timestamp) {
        try {
            String uid = SenzUtils.getUid(this, timestamp.toString());

            // save img in sdcard
            String imgName = uid + ".jpg";
            ImageUtils.saveImg(imgName, cheque.getImg());

            // create secret
            final Secret secret = new Secret("", BlobType.IMAGE, new SecretUser("id", cheque.getAccount()), false);
            secret.setId(uid);
            secret.setTimeStamp(timestamp);
            secret.setDeliveryState(DeliveryState.PENDING);
            new SenzorsDbSource(ChequePreviewActivity.this).createSecret(secret);

            // update unread count by one
            new SenzorsDbSource(ChequePreviewActivity.this).updateUnreadSecretCount(cheque.getAccount(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCheque(Long timestamp) {
        Senz senz = SenzUtils.getShareChequeSenz(this, cheque, timestamp);
        cheque.setId(senz.getId());
        send(senz);
    }
}
