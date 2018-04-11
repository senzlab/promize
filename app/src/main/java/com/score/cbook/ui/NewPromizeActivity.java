package com.score.cbook.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.enums.IntentType;
import com.score.cbook.exceptions.InvalidAmountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

public class NewPromizeActivity extends BaseActivity implements View.OnTouchListener {
    protected static final String TAG = NewPromizeActivity.class.getName();

    // camera related variables
    private Camera camera;
    private CameraPreview cameraPreview;

    // ui
    private RelativeLayout infoLayout;
    private LinearLayout amountContainer;
    private TextView amountHeader;
    private TextView rsHeader;
    private EditText amount;
    private FloatingActionButton capture;
    private FloatingActionButton send;
    private ImageView capturedPhoto;

    private ViewGroup rootLayout;
    private ImageView addSticker;
    private int _xDelta;
    private int _yDelta;

    // user
    private ChequeUser user;
    private Cheque cheque;

    private PowerManager.WakeLock wakeLock;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                handleSenz(senz);
            }
        }
    };

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Successfully sent", Toast.LENGTH_LONG).show();

                savePromize();
                this.finish();
            } else if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Failed to send iGift", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_promize_activity_layout);

        // init
        initUi();
        //initSignature();
        if (getIntent().hasExtra("USER")) this.user = getIntent().getParcelableExtra("USER");
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from service
        if (isServiceBound) {
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));

        // init camera with front
        acquireWakeLock();
        initCameraPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);

        releaseWakeLock();
        releaseCameraPreview();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUi() {
        infoLayout = (RelativeLayout) findViewById(R.id.amount_l);
        amountHeader = (TextView) findViewById(R.id.amount_header);
        rsHeader = (TextView) findViewById(R.id.rs);
        amount = (EditText) findViewById(R.id.new_cheque_amount);
        capturedPhoto = (ImageView) findViewById(R.id.capture_photo);

        amountHeader.setTypeface(typeface, Typeface.BOLD);
        rsHeader.setTypeface(typeface, Typeface.BOLD);
        amount.setTypeface(typeface, Typeface.BOLD);

        rootLayout = (ViewGroup) findViewById(R.id.relative_layout);
        addSticker = (ImageView) findViewById(R.id.add_sticker);
        addSticker.setVisibility(View.GONE);
        addSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewPromizeActivity.this, StickerListActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        amountContainer = (LinearLayout) findViewById(R.id.amount_container);
        amountContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return false;
            }
        });

        capture = (FloatingActionButton) findViewById(R.id.fab);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        send = (FloatingActionButton) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDone();
            }
        });

        send.setVisibility(View.GONE);
        capture.setVisibility(View.VISIBLE);
        infoLayout.setVisibility(View.GONE);
    }

    private void initSignature() {
        RelativeLayout signatureView = (RelativeLayout) findViewById(R.id.signature);

        Signature signature = new Signature(this, null);
        signatureView.addView(signature);
    }

    private void addSticker(int resourceId) {
        int w = (int) getResources().getDimension(R.dimen.imageview_width);
        int h = (int) getResources().getDimension(R.dimen.imageview_height);

        ImageView mImageView = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w, h);
        mImageView.setLayoutParams(layoutParams);
        mImageView.setImageResource(resourceId);
        rootLayout.addView(mImageView);
        mImageView.setOnTouchListener(this);
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SenzWakeLock");
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
        try {
            // render new preview
            camera = Camera.open(camFace);
            cameraPreview = new CameraPreview(this, camera, camFace);

            FrameLayout preview = (FrameLayout) findViewById(R.id.preview_frame);
            preview.addView(cameraPreview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCameraPreview() {
        try {
            if (camera != null) {
                cameraPreview.surfaceDestroyed(cameraPreview.getHolder());
                cameraPreview.getHolder().removeCallback(cameraPreview);
                cameraPreview.destroyDrawingCache();

                FrameLayout preview = (FrameLayout) findViewById(R.id.preview_frame);
                preview.removeView(cameraPreview);

                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePhoto() {
        // AudioUtil.shootSound(this);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                byte[] resizedImage = ImageUtil.compressImage(bytes, true, true);

                releaseCameraPreview();

                amount.requestFocus();

                // create bitmap and set to post capture
                Bitmap bitmap = ImageUtil.bytesToBmp(resizedImage);
                capturedPhoto.setImageBitmap(bitmap);

                send.setVisibility(View.VISIBLE);
                capture.setVisibility(View.GONE);
                addSticker.setVisibility(View.VISIBLE);
                animateView(infoLayout);
            }
        });
    }

    private byte[] captureView() {
        // create bitmap screen capture
        View v1 = findViewById(R.id.relative_layout);
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        // resize and save image
        Long t = System.currentTimeMillis();
        byte[] resizedImage = ImageUtil.compressImage(ImageUtil.bmpToBytes(bitmap), false, false);
        ImageUtil.saveImg(SenzUtil.getUid(this, t.toString() + ".jpg"), resizedImage);

        return resizedImage;
    }

    public void animateView(View view) {
        view.setVisibility(View.VISIBLE);

        Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in);
        view.startAnimation(a);
    }

    private void onClickDone() {
        try {
            ActivityUtil.isValidGift(amount.getText().toString().trim(), "");
            if (NetworkUtil.isAvailableNetwork(this)) {
                confirmPassword();
            } else {
                Toast.makeText(this, "No network connection", Toast.LENGTH_LONG).show();
            }
        } catch (InvalidInputFieldsException e) {
            displayInformationMessageDialog("Error", "Empty iGift amount");
            e.printStackTrace();
        } catch (InvalidAmountException e) {
            displayInformationMessageDialog("Error", "iGift amount should not exceed 100000 rupees");
            e.printStackTrace();
        }
    }

    private void confirmPassword() {
        final Dialog dialog = new Dialog(this);

        // set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.input_password_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // texts
        TextView title = (TextView) dialog.findViewById(R.id.title);
        final EditText password = (EditText) dialog.findViewById(R.id.password);
        title.setTypeface(typeface, Typeface.BOLD);
        password.setTypeface(typeface, Typeface.NORMAL);

        // set ok button
        Button done = (Button) dialog.findViewById(R.id.done);
        done.setTypeface(typeface, Typeface.BOLD);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().trim().equalsIgnoreCase(PreferenceUtil.getAccount(NewPromizeActivity.this).getPassword())) {
                    ActivityUtil.showProgressDialog(NewPromizeActivity.this, "Sending ...");
                    ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                    sendPromize(captureView(), amount.getText().toString());
                    dialog.cancel();
                } else {
                    Toast.makeText(NewPromizeActivity.this, "Invalid password", Toast.LENGTH_LONG).show();
                }
            }
        });

        // cancel button
        Button cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setTypeface(typeface, Typeface.BOLD);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void sendPromize(byte[] compBytes, String amount) {
        this.cheque = new Cheque();
        cheque.setUser(user);
        cheque.setAmount(amount);
        cheque.setDeliveryState(DeliveryState.PENDING);
        cheque.setChequeState(ChequeState.TRANSFER);
        cheque.setMyCheque(true);
        cheque.setViewed(true);
        cheque.setBlob(Base64.encodeToString(compBytes, Base64.DEFAULT));
        cheque.setAccount(PreferenceUtil.getAccount(this).getAccountNo());

        Long timestamp = System.currentTimeMillis() / 1000;
        cheque.setTimestamp(timestamp);

        Senz senz = SenzUtil.transferSenz(this, cheque, PreferenceUtil.getAccount(this));
        sendSenz(senz);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    addSticker(b.getInt("STICKER"));
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = 0;
                layoutParams.bottomMargin = 0;
                view.setLayoutParams(layoutParams);
                break;
        }
        rootLayout.invalidate();
        return true;
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