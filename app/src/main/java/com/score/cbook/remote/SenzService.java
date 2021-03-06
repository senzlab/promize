package com.score.cbook.remote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;

import com.score.cbook.application.IntentProvider;
import com.score.cbook.enums.IntentType;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.cbook.util.SmsUtil;
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

    //public static final String SENZ_HOST = "222.165.167.19";
    public static final String SENZ_HOST = "10.2.2.9";
    public static final int SENZ_PORT = 7171;

    // senz socket
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    // comm running
    private boolean running;

    // to wake up deep sleep mode
    private PowerManager.WakeLock senzWakeLock;

    // stubs that service expose(defines in ISenzService.aidl)
    private final ISenzService.Stub stubs = new ISenzService.Stub() {
        @Override
        public void sendSenz(Senz senz) throws RemoteException {
            writeSenz(senz);
        }

        @Override
        public void sendSenzes(List<Senz> senzList) throws RemoteException {
            writeSenzes(senzList);
        }
    };

    private BroadcastReceiver smsRequestAcceptReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationzHandler.cancel(context, NotificationzHandler.CUSTOMER_NOTIFICATION_ID);
            // send sms
            String phone = intent.getStringExtra("PHONE").trim();
            SmsUtil.sendAccept(SenzService.this, phone);

            // get pubkey
            String username = intent.getStringExtra("USERNAME").trim();
            writeSenz(SenzUtil.senzieKeySenz(SenzService.this, username));
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
            writeSenz(SenzUtil.senzieKeySenz(SenzService.this, username));
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return stubs;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // register receivers
        registerReceiver(smsRequestAcceptReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_ACCEPT));
        registerReceiver(smsRequestRejectReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_REJECT));
        registerReceiver(smsRequestConfirmReceiver, IntentProvider.getIntentFilter(IntentType.SMS_REQUEST_CONFIRM));

        // weak lock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        senzWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SenzWakeLock");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // un register receivers
        unregisterReceiver(smsRequestAcceptReceiver);
        unregisterReceiver(smsRequestRejectReceiver);
        unregisterReceiver(smsRequestConfirmReceiver);

        // release wake lock
        if (senzWakeLock != null && senzWakeLock.isHeld()) senzWakeLock.release();

        // restart service again via broadcast receiver
        Intent intent = new Intent(IntentProvider.ACTION_RESTART);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (running) {
            //tukSenz();
        } else {
            //new SenzCom().start();
        }

        return START_STICKY;
    }

    void tukSenz() {
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
                    // if sender not already set find user(sender) and set it to senz first
                    if (senz.getSender() == null || senz.getSender().isEmpty())
                        senz.setSender(PreferenceUtil.get(getBaseContext(), PreferenceUtil.Z_ADDRESS));

                    // get digital signature of the senz
                    PrivateKey privateKey = CryptoUtil.getPrivateKey(SenzService.this);
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
                    for (Senz senz : senzList) {
                        if (senz.getSender() == null || senz.getSender().isEmpty())
                            senz.setSender(PreferenceUtil.get(getBaseContext(), PreferenceUtil.Z_ADDRESS));

                        // get digital signature of the senz
                        PrivateKey privateKey = CryptoUtil.getPrivateKey(SenzService.this);
                        String senzPayload = SenzParser.compose(senz);
                        String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
                        String message = SenzParser.senzMsg(senzPayload, signature);

                        // sends the message to the server
                        write(message);
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
                reg();
                readCom();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeCom();
            }
        }

        private void initCom() throws IOException {
            socket = new Socket(InetAddress.getByName(SENZ_HOST), SENZ_PORT);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
        }

        private void reg() {
            String address = PreferenceUtil.get(SenzService.this, PreferenceUtil.Z_ADDRESS);
            if (!address.isEmpty()) writeSenz(SenzUtil.regSenz(SenzService.this, address));
        }

        private void readCom() throws IOException {
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


