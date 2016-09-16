package com.score.chatz.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.UserPermission;
import com.score.chatz.pojo.BitmapTaskParams;
import com.score.chatz.asyncTasks.BitmapWorkerTask;
import com.score.chatz.utils.NetworkUtil;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

public class UserProfileActivity extends BaseActivity {
    private static final String TAG = UserProfileActivity.class.getName();
    private User user;
    private TextView username;
    private Switch cameraSwitch;
    private Switch locationSwitch;
    private Switch micSwitch;
    private UserPermission userzPerm;
    private UserPermission currentUserGivenPerm;
    private User currentUser;
    private Button shareSecretBtn;
    private TextView tapImageText;
    private ImageView userImage;
    SenzorsDbSource dbSource;

    // service interface
    private ISenzService senzService = null;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected from senz service");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        String senderString = intent.getStringExtra("SENDER");
        user = new User("", senderString);

        username = (TextView) findViewById(R.id.user_name);
        userImage = (ImageView) findViewById(R.id.clickable_image);
        cameraSwitch = (Switch) findViewById(R.id.perm_camera_switch);
        micSwitch = (Switch) findViewById(R.id.perm_mic_switch);
        locationSwitch = (Switch) findViewById(R.id.perm_location_switch);

        userzPerm = getUserConfigPerm(user);
        currentUserGivenPerm = getUserAndPermission(user);

        dbSource = new SenzorsDbSource(this);

        try {
            currentUser = PreferenceUtils.getUser(this);
        } catch (NoUserException ex) {
            Log.e(TAG, "No registered user.");
        }

        setupGoToChatViewBtn();
        setupUserPermissions();
        registerAllReceivers();
        setupActionBar();
        setupGetProfileImageBtn();
        setupClickableImage();


    }

    private void setupGetProfileImageBtn() {
        tapImageText = (TextView) findViewById(R.id.tap_image_text);
        ImageView imgBtn = (ImageView) findViewById(R.id.clickable_image);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserGivenPerm.getCamPerm() == true) {
                    getPhoto(userzPerm.getUser());
                } else {
                    //Toast.makeText(UserProfileActivity.this, "Sorry. This user has not shared camera permissions with you.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     * Get photo of user
     */
    private void getPhoto(User receiver) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("profilezphoto", "profilezphoto");

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.GET;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setupClickableImage() {
        //Update permissions
        currentUserGivenPerm = getUserAndPermission(user);
        if (currentUserGivenPerm.getCamPerm() == true) {
            tapImageText.setVisibility(View.VISIBLE);
        } else {
            tapImageText.setVisibility(View.INVISIBLE);
        }
    }

    private void setupActionBar() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#636363")));
        getSupportActionBar().setTitle("@"+user.getUsername());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unbind from the service
        this.unbindService(senzServiceConnection);
        this.unregisterReceiver(userBusyNotifier);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(userBusyNotifier, new IntentFilter("com.score.chatz.USER_BUSY"));
    }

    private BroadcastReceiver userBusyNotifier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Senz senz = intent.getExtras().getParcelable("SENZ");
            displayInformationMessageDialog( getResources().getString(R.string.sorry),  senz.getSender().getUsername() + " " + getResources().getString(R.string.is_busy_now));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent();
        intent.setClassName("com.score.chatz", "com.score.chatz.services.RemoteSenzService");
        bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);


    }

    private void setupUserPermissions() {
        final Context context = getApplicationContext();
        userzPerm = getUserConfigPerm(user);

        username.setText(userzPerm.getUser().getUsername());

        if (userzPerm.getUser().getUserImage() != null) {
            loadBitmap(dbSource.getImageFromDB(user.getUsername()), userImage);
        }

        cameraSwitch.setChecked(userzPerm.getCamPerm());
        micSwitch.setChecked(userzPerm.getMicPerm());
        locationSwitch.setChecked(userzPerm.getLocPerm());
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (NetworkUtil.isAvailableNetwork(context)) {
                    if (isChecked == true) {
                        //Send permCam true to user
                        sendPermission(user, "true", null, null);
                    } else {
                        //Send permCam false to user
                        sendPermission(user, "false", null, null);
                    }
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (NetworkUtil.isAvailableNetwork(context)) {
                    if (isChecked == true) {
                        //Send permLoc true to user
                        sendPermission(user, null, "true", null);
                    } else {
                        //Send permLoc false to user
                        sendPermission(user, null, "false", null);
                    }
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        micSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (NetworkUtil.isAvailableNetwork(context)) {
                    if (isChecked == true) {
                        //Send permMic true to user
                        sendPermission(user, null, null, "true");
                    } else {
                        //Send permMic false to user
                        sendPermission(user, null, null, "false");
                    }
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadBitmap(String data, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (new BitmapTaskParams(data, 1000, 1000)));
        else
            task.execute(new BitmapTaskParams(data, 1000, 1000));
    }

    private UserPermission getUserConfigPerm(User user) {
        if (dbSource == null) {
            dbSource = new SenzorsDbSource(this);
            return dbSource.getUserConfigPermission(user);
        } else {
            return dbSource.getUserConfigPermission(user);
        }
    }

    private UserPermission getUserAndPermission(User user) {
        return dbSource.getUserAndPermission(user);
    }


    private void setupGoToChatViewBtn() {
        shareSecretBtn = (Button) findViewById(R.id.share_secret_btn);
        shareSecretBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("SENDER", user.getUsername());
                startActivity(intent);
            }
        });
    }


    public void sendPermission(User receiver, String camPerm, String locPerm, String micPerm) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("msg", "newPerm");
            if (camPerm != null) {
                senzAttributes.put("camPerm", camPerm);
            }
            if (locPerm != null) {
                senzAttributes.put("locPerm", locPerm);
            }
            if (micPerm != null) {
                senzAttributes.put("micPerm", micPerm);
            }
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);
            senz.setSender(currentUser);
            savePermToDB(senz);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void savePermToDB(Senz senz) {
        if (senz.getAttributes().containsKey("locPerm")) {
            dbSource.updateConfigurablePermissions(senz.getReceiver(), null, senz.getAttributes().get("locPerm"), null);
        }
        if (senz.getAttributes().containsKey("camPerm")) {
            dbSource.updateConfigurablePermissions(senz.getReceiver(), senz.getAttributes().get("camPerm"), null, null);
        }
        if (senz.getAttributes().containsKey("micPerm")) {
            dbSource.updateConfigurablePermissions(senz.getReceiver(), null, null, senz.getAttributes().get("micPerm"));
        }
    }

    private void registerAllReceivers() {
        registerReceiver(userSharedReceiver, new IntentFilter("com.score.chatz.USER_SHARED"));
        //Register for fail messages, incase other user is not online
        registerReceiver(senzDataReceiver, new IntentFilter("com.score.chatz.DATA_SENZ")); //Incoming data share
    }

    private void handleMessage(Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase("com.score.chatz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");
            if (senz.getAttributes().containsKey("msg")) {
                // msg response received
                String msg = senz.getAttributes().get("msg");
                if (msg != null && msg.equalsIgnoreCase("USER_NOT_ONLINE")) {
                    //Reset display
                    setupUserPermissions();
                }
            }

            String userImageData = dbSource.getImageFromDB(user.getUsername());
            if(userImageData != null)
            loadBitmap(userImageData, userImage);
            setupClickableImage();
            //updateActivity();
        }
    }


    private void updateActivity() {
        setupUserPermissions();
        setupClickableImage();
    }

    private void unregisterAllReceivers() {
        if (senzDataReceiver != null) unregisterReceiver(senzDataReceiver);
        if (userSharedReceiver != null) unregisterReceiver(userSharedReceiver);

    }


    private BroadcastReceiver userSharedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got user shared intent from Senz service");
            //handleSharedUser(intent);
        }
    };

    private BroadcastReceiver senzDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    private void handleSharedUser(Intent intent) {
        updateActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAllReceivers();
    }


}
