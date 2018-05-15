package com.score.cbook.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
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
import com.score.cbook.async.PostTask;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.exceptions.ExceedAmountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.InvalidMsgException;
import com.score.cbook.exceptions.LessAmountException;
import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.cbook.util.TimeUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;

public class NewPromizeActivity extends BaseActivity implements View.OnTouchListener, IPostTaskListener {
    protected static final String TAG = NewPromizeActivity.class.getName();

    // camera
    private Camera camera;
    private CameraPreview cameraPreview;
    private boolean isCameraOn;

    // root layouts
    private ViewGroup captureLayout;
    private FrameLayout previewLayout;

    // image panel
    private ImageView capturedPhoto;
    private FrameLayout overlayFrame;
    private int color = R.color.black;

    // amount panel
    private RelativeLayout infoPanel;
    private LinearLayout amountContainer;
    private TextView amountHeader;
    private TextView rsHeader;
    private EditText amount;

    // image/message panel
    private View stickerContainer;
    private RelativeLayout messageContainer;
    private EditText message;

    // buttons
    private FloatingActionButton capture;
    private FloatingActionButton send;
    private ImageView camClose;
    private ImageView addPhoto;
    private ImageView addText;
    private ImageView addSticker;
    private ImageView addBackground;

    // stickers
    private int xDelta;
    private int yDelta;

    // objs
    private ChequeUser user;
    private Cheque cheque;
    private Senz transferSenz;

    private PowerManager.WakeLock wakeLock;

    private void handleSenz(Senz senz) {
        if (senz.getSenzType() == SenzTypeEnum.DATA) {
            ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
            if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("SUCCESS")) {
            } else if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("ERROR")) {
                ActivityUtil.cancelProgressDialog();
                displayInformationMessageDialog("ERROR", "Failed to send iGift");
            } else if (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("DOUBLE_SPEND")) {
                ActivityUtil.cancelProgressDialog();
                Toast.makeText(this, "Successfully processed iGift", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_promize_activity_layout);

        // init
        initUi();
        resetTodayLimit();
        if (getIntent().hasExtra("USER")) this.user = getIntent().getParcelableExtra("USER");
        else this.user = new ChequeUser("era");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // init camera with front
        acquireWakeLock();
        initCameraPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseWakeLock();
        releaseCameraPreview();
        ActivityUtil.cancelProgressDialog();
    }

    private void initUi() {
        captureLayout = (ViewGroup) findViewById(R.id.capture_frame);
        previewLayout = (FrameLayout) findViewById(R.id.preview_frame);
        isCameraOn = false;

        stickerContainer = findViewById(R.id.sticker_container);
        capturedPhoto = (ImageView) findViewById(R.id.captured_photo);
        overlayFrame = (FrameLayout) findViewById(R.id.overlay_frame);

        infoPanel = (RelativeLayout) findViewById(R.id.amount_l);
        amountContainer = (LinearLayout) findViewById(R.id.amount_container);
        amountContainer.setOnTouchListener(this);
        amountHeader = (TextView) findViewById(R.id.amount_header);
        amountHeader.setTypeface(typeface, Typeface.BOLD);
        rsHeader = (TextView) findViewById(R.id.rs);
        rsHeader.setTypeface(typeface, Typeface.BOLD);
        amount = (EditText) findViewById(R.id.new_cheque_amount);
        amount.setTypeface(typeface, Typeface.BOLD);

        messageContainer = (RelativeLayout) findViewById(R.id.msg_bubble);
        message = (EditText) findViewById(R.id.message_text);
        message.setTypeface(typeface, Typeface.NORMAL);

        capture = (FloatingActionButton) findViewById(R.id.fab);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                capture();
            }
        });

        send = (FloatingActionButton) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                sendiGift();
            }
        });

        addPhoto = (ImageView) findViewById(R.id.add_photo);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                addPhoto();
            }
        });

        addText = (ImageView) findViewById(R.id.add_text);
        addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                addText();
            }
        });

        addSticker = (ImageView) findViewById(R.id.add_sticker);
        addSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                Intent intent = new Intent(NewPromizeActivity.this, StickerListActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        addBackground = (ImageView) findViewById(R.id.add_background);
        addBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                Intent intent = new Intent(NewPromizeActivity.this, BackgroundListActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        camClose = (ImageView) findViewById(R.id.cam_close);
        camClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                closeCam();
            }
        });

        addBackground(color);
        messageContainer.setVisibility(View.VISIBLE);

        capture.setVisibility(View.GONE);
        send.setVisibility(View.VISIBLE);
        addPhoto.setVisibility(View.VISIBLE);
        addText.setVisibility(View.VISIBLE);
        addSticker.setVisibility(View.VISIBLE);
        addBackground.setVisibility(View.VISIBLE);
        camClose.setVisibility(View.GONE);
        infoPanel.setVisibility(View.VISIBLE);
    }

    private void addPhoto() {
        isCameraOn = true;
        initCameraPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);

        addBackground(R.color.colorPrimaryTrans);

        send.setVisibility(View.GONE);
        capture.setVisibility(View.VISIBLE);
        addPhoto.setVisibility(View.GONE);
        addText.setVisibility(View.GONE);
        addSticker.setVisibility(View.GONE);
        addBackground.setVisibility(View.GONE);
        camClose.setVisibility(View.VISIBLE);

        infoPanel.setVisibility(View.GONE);
        Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_out);
        infoPanel.startAnimation(a);
    }

    private void addText() {
        if (messageContainer.getVisibility() == View.VISIBLE) {
            messageContainer.setVisibility(View.GONE);
        } else {
            messageContainer.setVisibility(View.VISIBLE);
        }
    }

    private void addSticker(int resourceId) {
        int w = (int) getResources().getDimension(R.dimen.imageview_width);
        int h = (int) getResources().getDimension(R.dimen.imageview_height);
        //int t = (stickerContainer.getHeight() - h) / 2;
        //int l = (stickerContainer.getWidth() - w) / 2;
        int t = 100;
        int l = 100;

        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w, h);
        layoutParams.topMargin = t;
        layoutParams.leftMargin = l;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(resourceId);
        captureLayout.addView(imageView);
        imageView.setOnTouchListener(this);
    }

    private void addBackground(int color) {
        overlayFrame.setBackgroundColor(getResources().getColor(color));
        capturedPhoto.setVisibility(View.GONE);
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
        if (isCameraOn) {
            try {
                camera = Camera.open(camFace);
                cameraPreview = new CameraPreview(this, camera, camFace);
                previewLayout.addView(cameraPreview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseCameraPreview() {
        if (isCameraOn) {
            try {
                if (camera != null) {
                    cameraPreview.surfaceDestroyed(cameraPreview.getHolder());
                    cameraPreview.getHolder().removeCallback(cameraPreview);
                    cameraPreview.destroyDrawingCache();
                    previewLayout.removeView(cameraPreview);

                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void capture() {
        // AudioUtil.shootSound(this);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                byte[] resizedImage = ImageUtil.compressImg(bytes, true, true);
                releaseCameraPreview();
                isCameraOn = false;

                capturedPhoto.setVisibility(View.VISIBLE);
                Bitmap bitmap = ImageUtil.bytesToBmp(resizedImage);
                capturedPhoto.setImageBitmap(bitmap);

                send.setVisibility(View.VISIBLE);
                capture.setVisibility(View.GONE);
                addPhoto.setVisibility(View.VISIBLE);
                addText.setVisibility(View.VISIBLE);
                addSticker.setVisibility(View.VISIBLE);
                addBackground.setVisibility(View.VISIBLE);
                camClose.setVisibility(View.GONE);

                infoPanel.setVisibility(View.VISIBLE);
                Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in);
                infoPanel.startAnimation(a);

                amount.requestFocus();
            }
        });
    }

    private void closeCam() {
        releaseCameraPreview();
        isCameraOn = false;

        addBackground(color);

        send.setVisibility(View.VISIBLE);
        capture.setVisibility(View.GONE);
        addPhoto.setVisibility(View.VISIBLE);
        addText.setVisibility(View.VISIBLE);
        addSticker.setVisibility(View.VISIBLE);
        addBackground.setVisibility(View.VISIBLE);
        camClose.setVisibility(View.GONE);

        infoPanel.setVisibility(View.VISIBLE);
        Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in);
        infoPanel.startAnimation(a);

        amount.requestFocus();
    }

    private void sendiGift() {
        try {
            String a = amount.getText().toString().trim();
            String m = message.getText().toString().trim();
            ActivityUtil.isValidGift(a, m);
            if (PreferenceUtil.get(this, PreferenceUtil.TODAY_AMOUNT, 0) + Integer.parseInt(a) > 10000) {
                displayInformationMessageDialog("ERROR", "Daily iGift transaction limit should be 10,000 rupees");
            } else {
                if (NetworkUtil.isAvailableNetwork(this)) askPassword();
                else Toast.makeText(this, "No network connection", Toast.LENGTH_LONG).show();
            }
        } catch (InvalidInputFieldsException e) {
            displayInformationMessageDialog("ERROR", "Empty iGift amount");
            e.printStackTrace();
        } catch (InvalidMsgException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "Please write iGift message to send");
        } catch (LessAmountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "iGift transaction amount should exceed 100 rupees");
        } catch (ExceedAmountException e) {
            e.printStackTrace();
            displayInformationMessageDialog("ERROR", "iGift transaction amount should not exceed 10,000 rupees");
        }
    }

    private void askPassword() {
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

        // ok button
        Button done = (Button) dialog.findViewById(R.id.done);
        done.setTypeface(typeface, Typeface.BOLD);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().trim().equalsIgnoreCase(PreferenceUtil.getAccount(NewPromizeActivity.this).getPassword())) {
                    dialog.cancel();
                    ActivityUtil.hideSoftKeyboard(NewPromizeActivity.this);
                    sendPromize(captureScreen(), amount.getText().toString());
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

    private byte[] captureScreen() {
        // create bitmap screen capture
        View v1 = findViewById(R.id.capture_frame);
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        return ImageUtil.compressImg(ImageUtil.bmpToBytes(bitmap), false, false);
    }

    private void sendPromize(byte[] compBytes, String amount) {
        try {
            if (transferSenz == null) {
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

                transferSenz = SenzUtil.transferSenz(this, cheque, PreferenceUtil.getAccount(this));
            }

            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(transferSenz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);

            // senz msg
            String uid = transferSenz.getAttributes().get("uid");
            String message = SenzParser.senzMsg(senzPayload, signature);
            SenzMsg senzMsg = new SenzMsg(uid, message);

            ActivityUtil.showProgressDialog(NewPromizeActivity.this, "Sending ...");
            PostTask task = new PostTask(this, PostTask.PROMIZE_API, senzMsg);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "POST");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePromize() {
        try {
            // save img in sdcard
            String uid = SenzUtil.getUid(this, cheque.getTimestamp().toString());
            String imgName = uid + ".jpg";
            ImageUtil.saveImg(imgName, cheque.getBlob());

            // create secret
            cheque.setUid(uid);
            ChequeSource.createCheque(this, cheque);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetTodayLimit() {
        String today = TimeUtil.today();
        if (!PreferenceUtil.get(this, PreferenceUtil.TODAY).equalsIgnoreCase(today)) {
            PreferenceUtil.put(this, PreferenceUtil.TODAY, today);
            PreferenceUtil.put(this, PreferenceUtil.TODAY_AMOUNT, 0);
        }
    }

    private void updateTodayLimit() {
        int todayAmount = PreferenceUtil.get(this, PreferenceUtil.TODAY_AMOUNT, 0);
        PreferenceUtil.put(this, PreferenceUtil.TODAY_AMOUNT, todayAmount + Integer.parseInt(cheque.getAmount()));
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
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    color = b.getInt("COLOR");
                    addBackground(color);
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == amountContainer) {
            if (message.isFocused()) message.clearFocus();
            if (amount.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(amount, InputMethodManager.SHOW_IMPLICIT);
            }
            return false;
        } else {
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = x - xDelta;
                    layoutParams.topMargin = y - yDelta;
                    layoutParams.rightMargin = 0;
                    layoutParams.bottomMargin = 0;
                    view.setLayoutParams(layoutParams);

                    if (layoutParams.topMargin > stickerContainer.getHeight()) {
                        captureLayout.removeView(view);
                    }
                    break;
            }
            captureLayout.invalidate();
            return true;
        }
    }

    @Override
    public void onFinishTask(Integer status) {
        ActivityUtil.cancelProgressDialog();
        ActivityUtil.hideSoftKeyboard(this);
        if (status == 200) {
            Toast.makeText(this, "Successfully sent iGift", Toast.LENGTH_LONG).show();

            updateTodayLimit();
            savePromize();
            this.finish();
        } else {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("ERROR", "Failed to send iGift");
        }
    }
}