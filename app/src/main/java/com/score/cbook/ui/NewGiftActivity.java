package com.score.cbook.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class NewGiftActivity extends BaseActivity {
    protected static final String TAG = NewGiftActivity.class.getName();

    // camera related variables
    private Camera camera;
    private CameraPreview cameraPreview;

    // ui
    private TextView sampathGift;
    private EditText from;
    private EditText to;
    private EditText amount;
    private FloatingActionButton capture;
    private FloatingActionButton send;
    private ImageView capturedPhoto;
    private FrameLayout previewFrame;
    private FrameLayout buttonFrame;
    private RelativeLayout giftInfo;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gift_activity_layout);

        // init camera with front
        acquireWakeLock();
        initCameraPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);

        // init activity
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
    protected void onDestroy() {
        super.onDestroy();

        releaseWakeLock();
        releaseCamera();
    }

    private void initUi() {
        sampathGift = (TextView) findViewById(R.id.sampath_gift);
        from = (EditText) findViewById(R.id.new_cheque_from);
        to = (EditText) findViewById(R.id.new_cheque_username);
        amount = (EditText) findViewById(R.id.new_cheque_amount);

        sampathGift.setTypeface(typeface, Typeface.BOLD);
        from.setTypeface(typeface, Typeface.BOLD);
        to.setTypeface(typeface, Typeface.BOLD);
        amount.setTypeface(typeface, Typeface.BOLD);

        giftInfo = (RelativeLayout) findViewById(R.id.gift_info);
        capturedPhoto = (ImageView) findViewById(R.id.capture_photo);

        capture = (FloatingActionButton) findViewById(R.id.capture);
        capture.setVisibility(View.VISIBLE);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        send = (FloatingActionButton) findViewById(R.id.send);
        send.setVisibility(View.GONE);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send
                send();
            }
        });
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

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = findViewById(R.id.relative_layout);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void takePhoto() {
        // AudioUtil.shootSound(this);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                byte[] resizedImage = ImageUtil.compressImage(bytes, true);

                releaseCameraPreview();

                // create bitmap and set to post capture
                Bitmap bitmap = ImageUtil.bytesToBmp(resizedImage);
                capturedPhoto.setImageBitmap(bitmap);
                capture.setVisibility(View.GONE);
                send.setVisibility(View.VISIBLE);
            }
        });
    }

    private void send() {
        send.setVisibility(View.GONE);
        disbaleEdit();
        takeScreenshot();
    }

    private void enableEdit() {
        from.setEnabled(true);
        to.setEnabled(true);
        amount.setEnabled(true);
    }

    private void disbaleEdit() {
        from.setEnabled(false);
        to.setEnabled(false);
        amount.setEnabled(false);
    }

    private void sendSelfieSenz(Long timestamp, String uid, String img) {
//        // create senz attributes
//        HashMap<String, String> senzAttributes = new HashMap<>();
//        senzAttributes.put("time", timestamp.toString());
//        senzAttributes.put("uid", uid);
//        senzAttributes.put("cam", img);
//
//        String id = "_ID";
//        String signature = "_SIG";
//        SenzTypeEnum senzType = SenzTypeEnum.DATA;
//        Senz senz = new Senz(id, signature, senzType, null, new User(secretUser.getId(), secretUser.getUsername()), senzAttributes);
//        send(senz);
    }

    private void saveSelfieSecret(Long timestamp, String uid, String image) {
//        Secret newSecret = new Secret("", BlobType.IMAGE, secretUser, false);
//        newSecret.setTimeStamp(timestamp);
//        newSecret.setId(uid);
//        newSecret.setMissed(false);
//        newSecret.setDeliveryState(DeliveryState.PENDING);
//        new SenzorsDbSource(SelfieCallAnswerActivity.this).createSecret(newSecret);
//
//        String imgName = uid + ".jpg";
//        ImageUtils.saveImg(imgName, image);
    }

}