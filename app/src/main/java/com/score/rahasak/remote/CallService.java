package com.score.rahasak.remote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.AudioUtils;
import com.score.rahasak.utils.CryptoUtils;
import com.score.rahasak.utils.OpusDecoder;
import com.score.rahasak.utils.OpusEncoder;
import com.score.rahasak.utils.PreferenceUtils;
import com.score.rahasak.utils.SenzUtils;
import com.score.rahasak.utils.VibrationUtils;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.crypto.SecretKey;


public class CallService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = CallService.class.getName();

    public static final int SAMPLE_RATE = 8000;
    public static final int FRAME_SIZE = 160;
    public static final int BUF_SIZE = FRAME_SIZE;

    private User appUser;
    private SecretUser secretUser;
    private SecretKey secretKey;

    // current audio setting
    private int audioMode;
    private int ringMode;
    private boolean isSpeakerPhoneOn;

    // we are listing for UDP socket
    private InetAddress address;
    private DatagramSocket recvSoc;
    private DatagramSocket sendSoc;

    // audio
    private AudioManager audioManager;

    // player/recorder and state
    private Player player;
    private Recorder recorder;

    private boolean calling;

    // senz message
    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");

            // extract senz
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                switch (senz.getSenzType()) {
                    case DATA:
                        onSenzReceived(senz);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        initPrefs(intent);
        initUdpSoc();
        initUdpConn();
        getAudioSettings();
        audioManager.requestAudioFocus(this, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);

        player = new Player();
        recorder = new Recorder();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        endCall();
        clrUdpConn();
        unregisterReceiver(senzReceiver);
        resetAudioSettings();
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange <= 0) {
            // loss audio focus
            // stop service
            stopSelf();
        }
    }

    private void initPrefs(Intent intent) {
        try {
            appUser = PreferenceUtils.getUser(this);
        } catch (NoUserException e) {
            e.printStackTrace();
        }

        if (intent.hasExtra("USER"))
            secretUser = intent.getParcelableExtra("USER");

        secretKey = CryptoUtils.getSecretKey(secretUser.getSessionKey());
    }

    private void initUdpSoc() {
        if (recvSoc == null || recvSoc.isClosed()) {
            try {
                recvSoc = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Socket already initialized");
        }
    }

    private void initUdpConn() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // connect
                    if (address == null)
                        address = InetAddress.getByName(SenzService.STREAM_HOST);

                    // send init message
                    String msg = SenzUtils.getStartStreamMsg(CallService.this, appUser.getUsername(), secretUser.getUsername());
                    if (msg != null) {
                        DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length(), address, SenzService.STREAM_PORT);
                        recvSoc.send(sendPacket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void clrUdpConn() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // send mic off
                    String senz = SenzUtils.getEndStreamMsg(CallService.this, appUser.getUsername(), secretUser.getUsername());
                    DatagramPacket sendPacket = new DatagramPacket(senz.getBytes(), senz.length(), address, SenzService.STREAM_PORT);
                    recvSoc.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onSenzReceived(Senz senz) {
        if (senz.getAttributes().containsKey("mic")) {
            if (senz.getAttributes().get("mic").equalsIgnoreCase("on")) {
                VibrationUtils.vibrate(this);
                startCall();
            } else if (senz.getAttributes().get("mic").equalsIgnoreCase("off")) {
                VibrationUtils.vibrate(this);
            }
        }
    }

    private void startCall() {
        calling = true;

        recorder.start();
        player.start();

        AudioUtils.enableEarpiece(CallService.this);
    }

    private void endCall() {
        calling = false;
    }

    private void getAudioSettings() {
        audioMode = audioManager.getMode();
        ringMode = audioManager.getRingerMode();
        isSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
    }

    private void resetAudioSettings() {
        audioManager.setMode(audioMode);
        audioManager.setRingerMode(ringMode);
        audioManager.setSpeakerphoneOn(isSpeakerPhoneOn);
    }

    /**
     * Player thread
     */
    private class Player implements Runnable {

        private final Thread thread;

        private AudioTrack streamTrack;

        private OpusDecoder opusDecoder;

        Player() {
            thread = new Thread(this);

            int minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            streamTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize,
                    AudioTrack.MODE_STREAM);
            Log.d(TAG, "AudioTrack min buffer size: ---- " + minBufSize);

            // init opus decoder
            opusDecoder = new OpusDecoder();
            opusDecoder.init(SAMPLE_RATE, 1, FRAME_SIZE);
        }

        public void start() {
            thread.start();
        }

        @Override
        public void run() {
            if (calling) {
                play();
            }
        }

        private void play() {
            streamTrack.play();

            try {
                short[] pcmframs = new short[BUF_SIZE];
                byte[] message = new byte[BUF_SIZE];

                while (calling) {
                    // listen for senz
                    DatagramPacket receivePacket = new DatagramPacket(message, message.length);
                    recvSoc.receive(receivePacket);
                    String msg = new String(message, 0, receivePacket.getLength());

                    // parser and obtain audio data
                    // play it
                    if (!msg.isEmpty()) {
                        // base64 decode
                        // decrypt
                        byte[] stream = CryptoUtils.decryptECB(secretKey, Base64.decode(msg, Base64.DEFAULT));
                        //byte[] stream = Base64.decode(msg, Base64.DEFAULT);

                        // decode codec
                        opusDecoder.decode(stream, pcmframs);
                        streamTrack.write(pcmframs, 0, pcmframs.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            opusDecoder.close();
            shutDown();
        }

        void shutDown() {
            if (streamTrack != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    streamTrack.pause();
                    streamTrack.flush();
                } else {
                    streamTrack.stop();
                }

                streamTrack = null;
            }
        }
    }

    /**
     * Recorder thread
     */
    private class Recorder implements Runnable {
        private final Thread thread;

        private AudioRecord audioRecorder;

        private OpusEncoder opusEncoder;

        Recorder() {
            thread = new Thread(this);

            int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            //if (minBufSize < 4096) {
            // opus require a 4KB buffer to work correctly
            //minBufSize = 4096;
            //}

            audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize);

            Log.d(TAG, " min buffer size: ---- " + minBufSize);

            // init opus encoder
            opusEncoder = new OpusEncoder();
            opusEncoder.init(SAMPLE_RATE, 1, FRAME_SIZE);
        }

        public void start() {
            thread.start();
        }

        @Override
        public void run() {
            if (calling) {
                record();
            }
        }

        private void record() {
            audioRecorder.startRecording();

            // enable
            // 1. AutomaticGainControl
            // 2. NoiseSuppressor
            // 3. AcousticEchoCanceler
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                AudioUtils.enableAGC(audioRecorder.getAudioSessionId());
                AudioUtils.enableNS(audioRecorder.getAudioSessionId());
                AudioUtils.enableAEC(audioRecorder.getAudioSessionId());
            }

            int encoded;
            short[] inBuf = new short[BUF_SIZE];
            byte[] outBuf = new byte[BUF_SIZE];

            while (calling) {
                // read to buffer
                // encode with codec
                audioRecorder.read(inBuf, 0, inBuf.length);
                encoded = opusEncoder.encode(inBuf, outBuf);

                try {
                    // encrypt
                    // base 64 encoded senz
                    String encodedStream = Base64.encodeToString(CryptoUtils.encryptECB(secretKey, outBuf, 0, encoded), Base64.DEFAULT).replaceAll("\n", "").replaceAll("\r", "");
                    //String encodedStream = Base64.encodeToString(outBuf, 0, encoded, Base64.DEFAULT).replaceAll("\n", "").replaceAll("\r", "");

                    String senz = encodedStream + " @" + secretUser.getUsername() + " ^" + appUser.getUsername();
                    sendStream(senz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            opusEncoder.close();
            shutDown();
        }

        private void sendStream(String senz) {
            try {
                if (sendSoc == null)
                    sendSoc = new DatagramSocket();
                DatagramPacket sendPacket = new DatagramPacket(senz.getBytes(), senz.length(), address, SenzService.STREAM_PORT);
                sendSoc.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void shutDown() {
            if (audioRecorder != null) {
                if (audioRecorder.getState() != AudioRecord.STATE_UNINITIALIZED)
                    audioRecorder.stop();
                audioRecorder.release();
                audioRecorder = null;
            }
        }
    }

}