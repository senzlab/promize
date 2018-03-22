package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class ChequePreviewActivity extends BaseActivity {

    private static final String TAG = ChequePActivity.class.getName();

    // ui controls
    private FloatingActionButton cancel;
    private FloatingActionButton done;
    private ImageView chqueImg;
    private RelativeLayout signatureView;
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

        // remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.cheque_p);

        initCheque();
        initUi();
        initSignature();
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

    private void initCheque() {
        chqueImg = (ImageView) findViewById(R.id.cheque_preview);

        this.cheque = getIntent().getParcelableExtra("CHEQUE");

        // load cheque on layout
        Bitmap cChq = ImageUtil.decodeBitmap(cheque.getBlob());
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
                ActivityUtil.showProgressDialog(ChequePreviewActivity.this, "Sending...");
                Long timestamp = System.currentTimeMillis() / 1000;
                cheque.setTimestamp(timestamp);
                signCheque();
                sendCheque();
            }
        });
    }

    private void initSignature() {
        signatureView = (RelativeLayout) findViewById(R.id.signature);

        Signature signature = new Signature(this, null);
        signatureView.addView(signature);
    }

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtil.cancelProgressDialog();

                // save cheque
                saveCheque();

                Toast.makeText(ChequePreviewActivity.this, "Successfully sent", Toast.LENGTH_LONG).show();
                ChequePreviewActivity.this.finish();
            } else {
                Toast.makeText(ChequePreviewActivity.this, "Fail to send", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void signCheque() {
        // signature on view
        Bitmap sig = Bitmap.createBitmap(signatureView.getWidth(), signatureView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sig);
        Drawable bgDrawable = signatureView.getBackground();
        bgDrawable.draw(canvas);
        signatureView.draw(canvas);

        // add signature to cheque
        Bitmap chq = ImageUtil.decodeBitmap(cheque.getBlob());
        Bitmap sChq = ImageUtil.addSign(chq, sig);

        // compress
        // set cheque
        byte[] bytes = ImageUtil.bmpToBytes(sChq);
        byte[] compBytes = ImageUtil.compressImage(bytes, false, false);
        cheque.setBlob(Base64.encodeToString(compBytes, Base64.DEFAULT));
    }

    private void saveCheque() {
        try {
            String uid = SenzUtil.getUid(this, cheque.getTimestamp().toString());

            // save img in sdcard
            String imgName = uid + ".jpg";
            ImageUtil.saveImg(imgName, cheque.getBlob());

            // create secret
            cheque.setUid(uid);
            ChequeSource.createCheque(this, cheque);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCheque() {
        Senz senz = SenzUtil.transferSenz(this, cheque, PreferenceUtil.getAccount(this));
        sendSenz(senz);
    }

    public class Signature extends View {
        static final float STROKE_WIDTH = 4f;
        static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        Paint paint = new Paint();
        Path path = new Path();

        float lastTouchX;
        float lastTouchY;
        final RectF dirtyRect = new RectF();

        public Signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}
