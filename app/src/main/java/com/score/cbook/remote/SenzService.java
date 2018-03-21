package com.score.cbook.remote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senz.ISenzService;
import com.score.senzc.pojos.Senz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.List;

public class SenzService extends Service {

    private static final String TAG = SenzService.class.getName();

    //public static final String SENZ_HOST = "www.rahasak.com";
    public static final String SENZ_HOST = "192.125.125.33";
    public static final int SENZ_PORT = 7171;

    public static final String SWITCH_NAME = "senzswitch";
    public static final String SAMPATH_AUTH_SENZIE_NAME = "sampath.auth";
    public static final String SAMPATH_CHAIN_SENZIE_NAME = "sampath.chain";
    public static final String SAMPATH_SUPPORT_SENZIE_NAME = "sampath.support";

    // wake lock to keep
    private PowerManager powerManager;
    private PowerManager.WakeLock senzWakeLock;

    // senz socket
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    // comm running
    private boolean running;

    // stubs that service expose(defines in ISenzService.aidl)
    private final ISenzService.Stub stubs = new ISenzService.Stub() {
        @Override
        public void send(Senz senz) throws RemoteException {
            writeSenz(senz);
        }

        @Override
        public void sendInOrder(List<Senz> senzList) throws RemoteException {
            writeSenzes(senzList);
        }

        @Override
        public void sendStream(Senz senz) throws RemoteException {
            writeStream(senz);
        }
    };

    private BroadcastReceiver smsRequestAcceptReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationzHandler.cancel(context, NotificationzHandler.CUSTOMER_NOTIFICATION_ID);

            if (NetworkUtil.isAvailableNetwork(context)) {
                try {
                    String phone = intent.getStringExtra("PHONE").trim();
                    String username = intent.getStringExtra("USERNAME").trim();
                    String address = PreferenceUtil.getSenzieAddress(SenzService.this);
                    sendSMS(phone, "#ChequeBook #confirm\nI have confirmed your request. #username " + address + " #code 31e3e");

                    // get pubkey
                    getSenzieKey(username);
                } catch (NoUserException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "No network connection", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver smsRequestRejectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationzHandler.cancel(context, NotificationzHandler.CUSTOMER_NOTIFICATION_ID);
        }
    };

    private BroadcastReceiver smsRequestConfirmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String username = intent.getStringExtra("USERNAME").trim();

            // get pubkey
            getSenzieKey(username);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return stubs;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceivers();
        initWakeLock();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (running) {
            // tuk
            tuk();
        } else {
            // reg
            new SenzCom().start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();

        // restart service again
        // its done via broadcast receiver
        Intent intent = new Intent(IntentProvider.ACTION_RESTART);
        sendBroadcast(intent);
    }

    private void registerReceivers() {
        registerReceiver(smsRequestAcceptReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_ACCEPT));
        registerReceiver(smsRequestRejectReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_REJECT));
        registerReceiver(smsRequestConfirmReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_CONFIRM));
    }

    private void unregisterReceivers() {
        // un register receivers
        unregisterReceiver(smsRequestAcceptReceiver);
        unregisterReceiver(smsRequestRejectReceiver);
        unregisterReceiver(smsRequestConfirmReceiver);
    }

    private void initWakeLock() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        senzWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SenzWakeLock");
    }

    private void ping() {
        try {
            String user = PreferenceUtil.getSenzieAddress(this);
            Senz senz = SenzUtil.regSenz(SenzService.this, user);
            writeSenz(senz);
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    private void getSenzieKey(String username) {
        Senz senz = SenzUtil.senzieKeySenz(this, username);
        writeSenz(senz);
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    void tuk() {
        new Thread(new Runnable() {
            public void run() {
                write("TUK");
            }
        }).start();
    }

    void writeSenz(final Senz senz) {
        new Thread(new Runnable() {
            public void run() {
                // sign and write senz
                try {
                    PrivateKey privateKey = CryptoUtil.getPrivateKey(SenzService.this);

                    // if sender not already set find user(sender) and set it to senz first
                    if (senz.getSender() == null || senz.getSender().isEmpty())
                        senz.setSender(PreferenceUtil.getSenzieAddress(getBaseContext()));

                    // get digital signature of the senz
                    String senzPayload = SenzParser.compose(senz);
                    String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
                    String message = SenzParser.senzMsg(senzPayload, signature);

                    //  sends the message to the server
                    write(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void writeSenzes(final List<Senz> senzList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrivateKey privateKey = CryptoUtil.getPrivateKey(SenzService.this);
                    String sender = PreferenceUtil.getSenzieAddress(SenzService.this);

                    for (Senz senz : senzList) {
                        senz.setSender(sender);

                        // get digital signature of the senz
                        String senzPayload = SenzParser.compose(senz);
                        String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);

                        // sends the message to the server
                        String message = SenzParser.senzMsg(senzPayload, signature);
                        write(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void writeStream(final Senz senz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set sender
                    if (senz.getSender() == null || senz.getSender().isEmpty())
                        senz.setSender(PreferenceUtil.getSenzieAddress(getBaseContext()));

                    // send img
                    if (senz.getAttributes().containsKey("img")) {
                        for (String packet : ImageUtil.splitImg(senz.getAttributes().get("img"), 1024))
                            write(packet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void write(String msg) {
        try {
            //  sends the message to the server
            if (socket != null) {
                outStream.writeBytes(msg + ";");
                outStream.flush();
            } else {
                Log.e(TAG, "Socket disconnected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SenzCom extends Thread {

        @Override
        public void run() {
            running = true;

            try {
                initCom();
                ping();
                readCom();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeCom();
            }
        }

        private void initCom() throws IOException {
            Log.d(TAG, "init socket");

            socket = new Socket(InetAddress.getByName(SENZ_HOST), SENZ_PORT);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
        }

        private void readCom() throws IOException {
            Log.d(TAG, "read socket");

            StringBuilder builder = new StringBuilder();
            int z;
            char c;
            while ((z = inStream.read()) != -1) {
                // obtain wake lock
                if (senzWakeLock != null && !senzWakeLock.isHeld()) senzWakeLock.acquire();

                c = (char) z;
                if (c == ';') {
                    String senz = builder.toString();
                    if (!senz.isEmpty()) {
                        Log.d(TAG, "Senz received " + senz);

                        builder = new StringBuilder();
                        SenzHandler.getInstance().handle(senz, SenzService.this);

                        // release wake lock
                        if (senzWakeLock != null && senzWakeLock.isHeld()) senzWakeLock.release();
                    }
                } else {
                    builder.append(c);
                }
            }
        }

        private void closeCom() {
            Log.d(TAG, "close comm");
            running = false;

            try {
                if (socket != null) {
                    socket.close();
                    inStream.close();
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


