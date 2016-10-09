package com.score.chatz.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.pojo.Secret;
import com.score.chatz.utils.AudioUtils;
import com.score.chatz.utils.ImageUtils;
import com.score.chatz.utils.SenzUtils;
import com.score.chatz.utils.VibrationUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoActivity extends BaseActivity implements View.OnTouchListener {
    protected static final String TAG = PhotoActivity.class.getName();

    // camera related variables
    private android.hardware.Camera mCamera;
    private CameraPreview mCameraPreview;
    private boolean isPhotoTaken;
    private boolean isPhotoCancelled;

    // UI elements
    private View callingUserInfo;
    private View buttonControls;
    private TextView quickCountdownText;
    private Rect startBtnRectRelativeToScreen;
    private Rect cancelBtnRectRelativeToScreen;
    private CircularImageView cancelBtn;
    private ImageView startBtn;
    private RippleBackground goRipple;

    // selfie request user
    private User user;

    private CountDownTimer cancelTimer;

    private static final int TIME_TO_SERVE_REQUEST = 10000;
    private static final int TIME_TO_QUICK_PHOTO = 3000;

    private float dX, dY, startX, startY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // user
        user = getIntent().getParcelableExtra("USER");

        //init camera
        initCameraPreview();
        initFlags();

        // init activity
        initUi();
        setupTitle();
        setupUserImage();
        initBtnAnimations();
        startVibrations();
        startTimerToEndRequest();
        startCameraIfMissedCall();
    }

    private void startCameraIfMissedCall() {
        if (getIntent().hasExtra("CAM_MIS")) {
            stopVibrations();
            cancelTimerToServe();
            startQuickCountdownToPhoto();
            isPhotoTaken = true;
        }
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

        clearFlags();
        stopVibrations();
        releaseCamera();
    }

    private void initFlags() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void clearFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initUi() {
        quickCountdownText = (TextView) findViewById(R.id.quick_count_down);
        quickCountdownText.setVisibility(View.INVISIBLE);
        callingUserInfo = findViewById(R.id.sender_info);
        buttonControls = findViewById(R.id.moving_layout);

        cancelBtn = (CircularImageView) findViewById(R.id.cancel);
        startBtn = (ImageView) findViewById(R.id.start);

        goRipple = (RippleBackground) findViewById(R.id.go_ripple);
        goRipple.setOnTouchListener(this);
    }

    private void setupTitle() {
        ((TextView) findViewById(R.id.photo_request_header)).setTypeface(typeface, Typeface.NORMAL);
        ((TextView) findViewById(R.id.photo_request_user_name)).setText(" @" + user.getUsername());
        ((TextView) findViewById(R.id.photo_request_user_name)).setTypeface(typeface, Typeface.NORMAL);
    }

    private void setupUserImage() {
        String userImage = new SenzorsDbSource(this).getImageFromDB(user.getUsername());
        if (userImage != null)
            ((ImageView) findViewById(R.id.user_profile_image)).setImageBitmap(new ImageUtils().decodeBitmap(userImage));
    }

    private void hideUiControls() {
        callingUserInfo.setVisibility(View.INVISIBLE);
        buttonControls.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            startBtnRectRelativeToScreen = new Rect(startBtn.getLeft(), startBtn.getTop(), startBtn.getRight(), startBtn.getBottom());
            cancelBtnRectRelativeToScreen = new Rect(cancelBtn.getLeft(), cancelBtn.getTop(), cancelBtn.getRight(), cancelBtn.getBottom());
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            Log.d(TAG, "Stopping preview in SurfaceDestroyed().");
            mCamera.release();
        }
    }

    private void initBtnAnimations() {
        Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
        goRipple.startRippleAnimation();
        goRipple.startAnimation(anim);
    }

    private void startVibrations() {
        VibrationUtils.startVibrationForPhoto(VibrationUtils.getVibratorPatterIncomingPhotoRequest(), this);
    }

    private void stopVibrations() {
        VibrationUtils.stopVibration(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                v.clearAnimation();
                startX = v.getX();
                startY = v.getY();
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();

                break;

            case (MotionEvent.ACTION_MOVE):
                v.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                if (startBtnRectRelativeToScreen.contains((int) (event.getRawX()), (int) (event.getRawY()))) {
                    // Inside start button region
                    stopVibrations();
                    if (!isPhotoTaken) {
                        cancelTimerToServe();
                        startQuickCountdownToPhoto();
                        isPhotoTaken = true;
                    }
                } else if (cancelBtnRectRelativeToScreen.contains((int) (event.getRawX()), (int) (event.getRawY()))) {
                    // Inside cancel button region
                    if (!isPhotoCancelled) {
                        isPhotoCancelled = true;
                        cancelTimerToServe();
                        sendBusySenz();
                        stopVibrations();
                        saveMissedSelfie();
                        this.finish();
                    }
                }
                break;
            case (MotionEvent.ACTION_UP):
                v.animate()
                        .x(startX)
                        .y(startY)
                        .setDuration(0)
                        .start();
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
                v.startAnimation(anim);
                break;
        }
        return true;
    }

    private void startQuickCountdownToPhoto() {
        quickCountdownText.setVisibility(View.VISIBLE);
        hideUiControls();

        new CountDownTimer(TIME_TO_QUICK_PHOTO, 1000) {
            public void onTick(long millisUntilFinished) {
                updateQuicCountTimer((int) millisUntilFinished / 1000);
            }

            public void onFinish() {
                takePhoto();
            }
        }.start();
    }

    /**
     * Helper method to access the camera returns null if
     * it cannot get the camera or does not exist
     *
     * @return
     */
    private void initCameraPreview() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCameraPreview = new CameraPreview(this, mCamera);

            FrameLayout preview = (FrameLayout) findViewById(R.id.photo);
            preview.addView(mCameraPreview);
        } catch (Exception e) {
            // cannot get camera or does not exist
            Log.e(TAG, "No font cam");
        }
    }

    private void takePhoto() {
        quickCountdownText.setVisibility(View.INVISIBLE);
        AudioUtils.shootSound(this);
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                byte[] resizedImage = new ImageUtils().compressImage(bytes);
                sendPhotoSenz(resizedImage, PhotoActivity.this);
                finish();
            }
        });
    }

    private void updateQuicCountTimer(final int count) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                quickCountdownText.setText(count + "");
            }
        });
    }

    private void startTimerToEndRequest() {
        cancelTimer = new CountDownTimer(TIME_TO_SERVE_REQUEST, TIME_TO_SERVE_REQUEST) {
            @Override
            public void onFinish() {
                sendBusySenz();
                saveMissedSelfie();
                PhotoActivity.this.finish();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Time in count down -" + millisUntilFinished);
            }
        }.start();
    }

    private void saveMissedSelfie() {
        Long timestamp = (System.currentTimeMillis() / 1000);
        String uid = SenzUtils.getUid(this, timestamp.toString());
        Secret newSecret = new Secret("", "IMAGE", user, true);
        newSecret.setTimeStamp(timestamp);
        newSecret.setId(uid);
        newSecret.setMissed(true);
        new SenzorsDbSource(this).createSecret(newSecret);
    }

    private void sendBusySenz() {
        Long timestamp = (System.currentTimeMillis() / 1000);
        String uid = SenzUtils.getUid(this, timestamp.toString());

        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", uid);
        senzAttributes.put("status", "801");

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Senz _senz = new Senz(id, signature, senzType, null, user, senzAttributes);
        send(_senz);
    }

    private void cancelTimerToServe() {
        if (cancelTimer != null)
            cancelTimer.cancel();
    }

    /**
     * Util methods
     */
    private void sendPhotoSenz(final byte[] image, final Context context) {
        // compose senz
        Long timestamp = (System.currentTimeMillis() / 1000);
        String uid = SenzUtils.getUid(this, timestamp.toString());

        // stream on senz
        // stream content
        // stream off senz
        Senz startStreamSenz = getStartStreamSenz(uid, timestamp);
        ArrayList<Senz> photoSenzList = getPhotoStreamSenz(image, context, uid, timestamp);
        Senz stopStreamSenz = getStopStreamSenz(uid, timestamp);

        // populate list
        ArrayList<Senz> senzList = new ArrayList<>();
        senzList.add(startStreamSenz);
        senzList.addAll(photoSenzList);
        senzList.add(stopStreamSenz);

        sendInOrder(senzList);
    }

    /**
     * Decompose image stream in to multiple data/stream senz's
     *
     * @param image   image content
     * @param context app context
     * @param uid     unique id
     * @return list of decomposed senz's
     */
    private ArrayList<Senz> getPhotoStreamSenz(byte[] image, Context context, String uid, Long timestamp) {
        String imageString = new ImageUtils().encodeBitmap(image);

        Secret newSecret = new Secret(imageString, "IMAGE", user, false);
        newSecret.setTimeStamp(timestamp);
        newSecret.setId(uid);
        newSecret.setMissed(false);
        new SenzorsDbSource(context).createSecret(newSecret);

        ArrayList<Senz> senzList = new ArrayList<>();
        String[] packets = split(imageString, 1024);

        for (String packet : packets) {
            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.STREAM;

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", timestamp.toString());
            senzAttributes.put("cam", packet.trim());
            senzAttributes.put("uid", uid);

            Senz _senz = new Senz(id, signature, senzType, null, user, senzAttributes);
            senzList.add(_senz);
        }

        return senzList;
    }

    /**
     * Create start stream senz
     *
     * @return senz
     */
    private Senz getStartStreamSenz(String uid, Long timestamp) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("cam", "on");
        senzAttributes.put("uid", uid);

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.STREAM;

        return new Senz(id, signature, senzType, null, user, senzAttributes);
    }

    /**
     * Create stop stream senz
     *
     * @return senz
     */
    private Senz getStopStreamSenz(String uid, Long timestamp) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("cam", "off");
        senzAttributes.put("uid", uid);

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.STREAM;

        return new Senz(id, signature, senzType, null, user, senzAttributes);
    }

    private String[] split(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
    }

}