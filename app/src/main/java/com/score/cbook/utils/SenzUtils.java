package com.score.cbook.utils;

import android.content.Context;

import com.score.cbook.exceptions.NoUserException;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.remote.SenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SenzUtils {
    public static Senz getRegSenz(Context context) {
        try {
            User user = PreferenceUtils.getUser(context);

            // create create senz
            HashMap<String, String> senzAttributes = new HashMap<>();

            Long timestamp = System.currentTimeMillis() / 1000;
            senzAttributes.put("time", timestamp.toString());
            senzAttributes.put("uid", getUid(context, timestamp.toString()));
            senzAttributes.put("pubkey", PreferenceUtils.getRsaKey(context, CryptoUtils.PUBLIC_KEY));

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.SHARE);
            senz.setSender(new User("", user.getUsername()));
            senz.setReceiver(new User("", SenzService.SWITCH_NAME));
            senz.setAttributes(senzAttributes);

            return senz;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Senz getPubkeySenz(Context context, String user) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();

        Long timestamp = System.currentTimeMillis() / 1000;
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("pubkey", "");
        senzAttributes.put("name", user);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.GET);
        senz.setReceiver(new User("", SenzService.SWITCH_NAME));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getShareAckSenz(Context context, User user, String statusCode) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis() / 1000;
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("status", statusCode);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(user);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getAwaSenz(String uid) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("uid", uid);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.AWA);
        senz.setReceiver(new User("", SenzService.SWITCH_NAME));
        senz.setAttributes(senzAttributes);

        return senz;
    }


    public static Senz getShareSenz(Context context, String username, String sessionKey) throws NoSuchAlgorithmException {
        // create senz attributes
        Long timestamp = (System.currentTimeMillis() / 1000);
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("msg", "");
        senzAttributes.put("status", "");
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", SenzUtils.getUid(context, timestamp.toString()));

        // put session key
        senzAttributes.put("$skey", sessionKey);

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(new User("", username));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getShareChequeSenz(Context context, Cheque cheque, Long timestamp) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("camnt", Integer.toString(cheque.getAmount()));
        senzAttributes.put("cdate", cheque.getDate());
        senzAttributes.put("cbnk", "sampath");
        senzAttributes.put("cimg", cheque.getBlob());
        senzAttributes.put("to", cheque.getUser().getUsername());
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", SenzUtils.getUid(context, timestamp.toString()));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(new User("", SenzService.SAMPATH_CHAIN_SENZIE_NAME));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getDepositChequeSenz(Context context, Cheque cheque, Long timestamp) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("camnt", Integer.toString(cheque.getAmount()));
        senzAttributes.put("cbnk", "sampath");
        senzAttributes.put("to", "sampath");
        senzAttributes.put("cid", cheque.getCid());
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", SenzUtils.getUid(context, timestamp.toString()));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(new User("", SenzService.SAMPATH_CHAIN_SENZIE_NAME));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getSenzFromCheque(Context context, Cheque cheque) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();

        // TODO set new timestamp and uid
        // TODO update them in db
        //Long timestamp = (System.currentTimeMillis() / 1000);
        //String uid = SenzUtils.getUid(context, timestamp.toString());
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", cheque.getUid());
        senzAttributes.put("user", cheque.getUser().getUsername());
        if (cheque.getUser().getSessionKey() != null && !cheque.getUser().getSessionKey().isEmpty()) {
            senzAttributes.put("$msg", CryptoUtils.encryptECB(CryptoUtils.getSecretKey(cheque.getUser().getSessionKey()), cheque.getBlob()));
        } else {
            senzAttributes.put("msg", cheque.getBlob());
        }

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(new User("", cheque.getUser().getUsername()));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static String getUid(Context context, String timestamp) {
        try {
            String username = PreferenceUtils.getUser(context).getUsername();
            return username + timestamp;
        } catch (NoUserException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

}
