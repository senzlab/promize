package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.application.SenzApplication;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.LimitedList;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.SenzUtil;
import com.score.cbook.util.TimeUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends BaseActivity {

    // UI components
    private EditText txtSecret;
    private TextView btnSend;
    private Toolbar toolbar;

    // secret list
    private ListView listView;
    private ChatListAdapter secretAdapter;

    private ChequeUser chequeUser;
    private LimitedList<Secret> secretList;

    // senz received
    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                switch (senz.getSenzType()) {
                    case DATA:
                        if (senz.getAttributes().containsKey("msg")) onSenzMsg(senz);
                        break;
                    case AWA:
                        updateStatus(senz.getAttributes().get("uid"), "RECEIVED");
                        break;
                    case GIYA:
                        updateStatus(senz.getAttributes().get("uid"), "DELIVERED");
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        initUi();
        initUser();
        setupToolbar();
        setupActionBar();
        initSecretList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initUser(intent);
        setupActionBar();
        initSecretList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindFromService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on chat
        SenzApplication.setOnChat(true);
        SenzApplication.setChatUser(chequeUser.getUsername());

        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));

        refreshSecretList();
    }

    @Override
    public void onPause() {
        super.onPause();

        // not on chat
        SenzApplication.setOnChat(false);
        SenzApplication.setChatUser(null);

        unregisterReceiver(senzReceiver);
    }

    private void initUi() {
        txtSecret = (EditText) findViewById(R.id.text_message);
        txtSecret.setTypeface(typeface, Typeface.NORMAL);

        btnSend = (TextView) findViewById(R.id.sendBtn);
        btnSend.setTypeface(typeface, Typeface.BOLD);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSend();
            }
        });

        listView = (ListView) findViewById(R.id.messages_list_view);
        listView.setDivider(null);
        listView.setDividerHeight(0);
    }

    private void initUser() {
        String username = getIntent().getExtras().getString("SENDER");
        chequeUser = UserSource.getUser(this, username);
    }

    private void initUser(Intent intent) {
        String username = intent.getStringExtra("SENDER");
        chequeUser = UserSource.getUser(this, username);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.new_cheque_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        if (chequeUser.getPhone() != null && !chequeUser.getPhone().isEmpty()) {
            titleText.setText(PhoneBookUtil.getContactName(getApplicationContext(), chequeUser.getPhone()));
        } else {
            titleText.setText("@" + chequeUser.getUsername());
        }

        // back button
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // profile button
        ImageView profileBtn = (ImageView) findViewById(R.id.user_profile_image);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate ro profile
            }
        });

        // show bank logo for admin users
        // show profile image for other users
        if (chequeUser.isAdmin()) {
            profileBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_sampath));
        } else {
            if (chequeUser.getImage() != null)
                profileBtn.setImageBitmap(ImageUtil.decodeBitmap(chequeUser.getImage()));
        }
    }

    private void initSecretList() {
        ArrayList<Secret> tmpList = SecretSource.getSecretsOfUser(this, chequeUser.getUsername());
        secretList = new LimitedList<>(tmpList.size());
        secretList.addAll(tmpList);

        secretAdapter = new ChatListAdapter(this, chequeUser, secretList);
        listView.setAdapter(secretAdapter);
    }

    private void refreshSecretList() {
        if (!secretList.isEmpty()) {
            ArrayList<Secret> tmpList = SecretSource.getSecretsOfUserByTime(this, chequeUser.getUsername(), secretList.getYongest().getTimeStamp());
            if (tmpList.size() > 0) {
                // add new secrets
                for (Secret secret : tmpList) {
                    secretList.add(secret);
                }
                secretAdapter.notifyDataSetChanged();

                // move to bottom
                listView.post(new Runnable() {
                    public void run() {
                        listView.smoothScrollToPosition(listView.getCount() - 1);
                    }
                });
            }
        }
    }

    private void addSecret(Secret secret) {
        // visible/hide previous chat status according to time diff
        Secret preSecret = secretList.getYongest();
        if (preSecret != null && preSecret.isMySecret() == secret.isMySecret() && TimeUtil.isInOrder(preSecret.getTimeStamp(), secret.getTimeStamp())) {
            // time diff less than 1 min, so hide status of previous chat
            preSecret.setInOrder(true);
        }

        // update list view
        secretList.add(secret);
        secretAdapter.notifyDataSetChanged();
        listView.post(new Runnable() {
            public void run() {
                listView.smoothScrollToPosition(listView.getCount() - 1);
            }
        });
    }

    private void onClickSend() {
        String secretMsg = txtSecret.getText().toString().trim();
        if (!secretMsg.isEmpty()) {
            // clear text
            txtSecret.setText("");

            // create secret
            Secret secret = new Secret();
            secret.setBlobType(BlobType.TEXT);
            secret.setBlob(secretMsg);
            secret.setUser(chequeUser);
            secret.setMySecret(true);
            Long timestamp = System.currentTimeMillis() / 1000;
            secret.setTimeStamp(timestamp);
            secret.setId(SenzUtil.getUid(this, timestamp.toString()));
            secret.setDeliveryState(DeliveryState.PENDING);

            // send secret
            // save secret
            sendSecret(secret);
            SecretSource.createSecret(this, secret);

            // add secret
            addSecret(secret);
        }
    }

    private void sendSecret(Secret secret) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();

            // encrypt msg
            if (chequeUser.getSessionKey() != null && !chequeUser.getSessionKey().isEmpty()) {
                senzAttributes.put("$msg", CryptoUtil.encryptECB(CryptoUtil.getSecretKey(chequeUser.getSessionKey()), secret.getBlob()));
            } else {
                senzAttributes.put("msg", URLEncoder.encode(secret.getBlob(), "UTF-8"));
            }

            String timeStamp = ((Long) (System.currentTimeMillis() / 1000)).toString();
            senzAttributes.put("time", timeStamp);
            senzAttributes.put("uid", secret.getId());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz senz = new Senz(id, signature, senzType, null, chequeUser.getUsername(), senzAttributes);

            sendSenz(senz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSenzMsg(Senz senz) {
        if (senz.getSender().equalsIgnoreCase(chequeUser.getUsername())) {
            if (senz.getAttributes().containsKey("msg")) {
                String msg = senz.getAttributes().get("msg");

                Secret secret = new Secret();
                secret.setBlobType(BlobType.TEXT);
                secret.setBlob(msg);
                secret.setUser(chequeUser);
                secret.setMySecret(false);
                secret.setTimeStamp(Long.parseLong(senz.getAttributes().get("time")));
                secret.setId(senz.getAttributes().get("uid"));
                secret.setDeliveryState(DeliveryState.PENDING);

                addSecret(secret);
            }
        }
    }

    private void updateStatus(String uid, String status) {
        if (status.equalsIgnoreCase("DELIVERED") || status.equalsIgnoreCase("RECEIVED")) {
            for (Secret secret : secretList) {
                if (secret.getId().equalsIgnoreCase(uid)) {
                    secret.setDeliveryState(DeliveryState.valueOf(status));
                    secretAdapter.notifyDataSetChanged();
                }
            }
        }
    }

}
