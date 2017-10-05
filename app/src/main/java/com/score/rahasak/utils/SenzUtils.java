package com.score.rahasak.utils;

import android.content.Context;

import com.score.rahasak.exceptions.NoUserException;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.pojo.Secret;
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
    public static Senz getPingSenz(Context context) {
        try {
            User user = PreferenceUtils.getUser(context);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz object
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.PING);
            senz.setSender(new User("", user.getUsername()));
            senz.setReceiver(new User("", "senzswitch"));
            senz.setAttributes(senzAttributes);

            return senz;
        } catch (NoUserException e) {
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
        senz.setReceiver(new User("", "senzswitch"));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getAckSenz(User user, String uid, String statusCode) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("uid", uid);
        senzAttributes.put("status", statusCode);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(user);
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
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.SHARE;
        User receiver = new User("", username);
        Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

        // send to service
        return senz;
    }

    public static Senz getSenzFromSecret(Context context, Secret secret) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();

        // TODO set new timestamp and uid
        // TODO update them in db
        //Long timestamp = (System.currentTimeMillis() / 1000);
        //String uid = SenzUtils.getUid(context, timestamp.toString());
        senzAttributes.put("time", secret.getTimeStamp().toString());
        senzAttributes.put("uid", secret.getId());
        senzAttributes.put("user", secret.getUser().getUsername());
        if (secret.getUser().getSessionKey() != null && !secret.getUser().getSessionKey().isEmpty()) {
            senzAttributes.put("$msg", CryptoUtils.encryptECB(CryptoUtils.getSecretKey(secret.getUser().getSessionKey()), secret.getBlob()));
        } else {
            senzAttributes.put("msg", secret.getBlob());
        }

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(new User("", secret.getUser().getUsername()));
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz getShareChequeSenz(Context context, Cheque cheque) {
        // create senz attributes
        Long timestamp = (System.currentTimeMillis() / 1000);
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("camnt", Integer.toString(cheque.getAmount()));
        senzAttributes.put("cbnk", "sampath");
        senzAttributes.put("cimg", cheque.getImg());
        senzAttributes.put("to", cheque.getAccount());
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", SenzUtils.getUid(context, timestamp.toString()));

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.SHARE;
        User receiver = new User("", "sampath");
        Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

        // send to service
        return senz;
    }

}
