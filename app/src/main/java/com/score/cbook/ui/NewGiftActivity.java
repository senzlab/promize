package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class NewGiftActivity extends BaseActivity {
    protected static final String TAG = NewGiftActivity.class.getName();

    // camera related variables
    private Camera camera;
    private CameraPreview cameraPreview;

    // ui
    private TextView sampathGift;
    private EditText amount;
    private FloatingActionButton send;
    private ImageView capturedPhoto;

    private FrameLayout previewFrame;

    // user
    private ChequeUser user;
    private Cheque cheque;

    private PowerManager.WakeLock wakeLock;

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

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                // share success
                ActivityUtil.cancelProgressDialog();

                // save cheque
                savePromize();

                Toast.makeText(this, "Successfully sent", Toast.LENGTH_LONG).show();
                this.finish();
            } else {
                Toast.makeText(this, "Fail to send", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gift_activity_layout);

        // init camera with front
        acquireWakeLock();
        initCameraPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);

        // init
        initUi();
        initPrefs();
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
    protected void onDestroy() {
        super.onDestroy();

        releaseWakeLock();
        releaseCamera();
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

    private void initUi() {
        sampathGift = (TextView) findViewById(R.id.sampath_gift);
        amount = (EditText) findViewById(R.id.new_cheque_amount);
        capturedPhoto = (ImageView) findViewById(R.id.capture_photo);

        sampathGift.setTypeface(typeface, Typeface.BOLD);
        amount.setTypeface(typeface, Typeface.BOLD);

        send = (FloatingActionButton) findViewById(R.id.fab);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send
                onClickSend();
            }
        });
    }

    private void initPrefs() {
        if (getIntent().hasExtra("USER"))
            this.user = getIntent().getParcelableExtra("USER");
    }

    private void onClickSend() {
        prepareView();
        ActivityUtil.showProgressDialog(this, "Sending ...");
        takePhoto();
        sendPromize(captureView());
    }

    private void prepareView() {
        amount.setEnabled(false);
    }

    private void releaseCamera() {
        if (camera != null) {
            Log.d(TAG, "Stopping preview in SurfaceDestroyed().");
            camera.release();
        }
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "senz");
        wakeLock.acquire();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void releaseWakeLock() {
        wakeLock.release();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initCameraPreview(int camFace) {
        releaseCameraPreview();

        // render new preview
        try {
            camera = Camera.open(camFace);
            cameraPreview = new CameraPreview(this, camera, camFace);

            FrameLayout preview = (FrameLayout) findViewById(R.id.preview_frame);
            preview.addView(cameraPreview);
        } catch (Exception e) {
            // cannot get camera or does not exist
            e.printStackTrace();
            Log.e(TAG, "No font cam");
        }
    }

    private void releaseCameraPreview() {
        if (cameraPreview != null) {
            cameraPreview.surfaceDestroyed(cameraPreview.getHolder());
            cameraPreview.getHolder().removeCallback(cameraPreview);
            cameraPreview.destroyDrawingCache();

            FrameLayout preview = (FrameLayout) findViewById(R.id.preview_frame);
            preview.removeView(cameraPreview);

            camera.stopPreview();
            camera.release();
        }
    }

    private void takePhoto() {
        // AudioUtil.shootSound(this);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                byte[] resizedImage = ImageUtil.compressImage(bytes, true, true);

                releaseCameraPreview();

                // create bitmap and set to post capture
                Bitmap bitmap = ImageUtil.bytesToBmp(resizedImage);
                capturedPhoto.setImageBitmap(bitmap);
                send.setVisibility(View.GONE);
            }
        });
    }

    private byte[] captureView() {
        // create bitmap screen capture
        View v1 = findViewById(R.id.capture_frame);
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        // resize and save image
        Long t = System.currentTimeMillis();
        byte[] resizedImage = ImageUtil.compressImage(ImageUtil.bmpToBytes(bitmap), false, false);
        ImageUtil.saveImg(SenzUtil.getUid(this, t.toString() + ".jpg"), resizedImage);

        return resizedImage;
    }

    private void sendPromize(byte[] compBytes) {
        this.cheque = new Cheque();
        cheque.setUser(user);
        cheque.setAmount(3000);
        cheque.setDate("12/03/2018");
        cheque.setDeliveryState(DeliveryState.PENDING);
        cheque.setChequeState(ChequeState.TRANSFER);
        cheque.setMyCheque(true);
        cheque.setViewed(true);
        cheque.setBlob(Base64.encodeToString(compBytes, Base64.DEFAULT));

        Long timestamp = System.currentTimeMillis() / 1000;
        cheque.setTimestamp(timestamp);

        Senz senz = SenzUtil.transferChequeSenz(this, cheque, cheque.getTimestamp());
        send(senz);
    }

    private void savePromize() {
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

}