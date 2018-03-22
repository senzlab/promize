package com.score.cbook.util;

import android.content.Context;

import com.score.cbook.pojo.Account;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.remote.SenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SenzUtil {
    public static Senz regSenz(Context context, String sender) {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        String uid = timestamp.toString() + sender;
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", uid);
        senzAttributes.put("pubkey", PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setSender(sender);
        senz.setReceiver(SenzService.SWITCH_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz authSenz(Context context, String sender) {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        String uid = timestamp.toString() + sender;
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", uid);
        senzAttributes.put("pubkey", PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setSender(sender);
        senz.setReceiver(SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz accountSenz(Context context, String account) {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("acc", account);

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz saltSenz(Context context, String salt) {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("salt", salt);

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz senzieKeySenz(Context context, String user) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("pubkey", "");
        senzAttributes.put("name", user);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.GET);
        senz.setReceiver(SenzService.SWITCH_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz shareSenz(Context context, String receiver, String sessionKey) throws NoSuchAlgorithmException {
        // create senz attributes
        Long timestamp = System.currentTimeMillis();
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("msg", "");
        senzAttributes.put("status", "");
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", SenzUtil.getUid(context, timestamp.toString()));

        // put session key
        senzAttributes.put("$skey", sessionKey);

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(receiver);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz statusSenz(Context context, String receiver, String statusCode) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", getUid(context, timestamp.toString()));
        senzAttributes.put("status", statusCode);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(receiver);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz awaSenz(String uid) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", uid);

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.AWA);
        senz.setReceiver(SenzService.SWITCH_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz transferSenz(Context context, Cheque cheque, Account account) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("amnt", Integer.toString(cheque.getAmount()));
        senzAttributes.put("bnk", account.getBank());
        senzAttributes.put("acc", account.getAccountNo());
        senzAttributes.put("blob", cheque.getBlob());
        senzAttributes.put("to", cheque.getUser().getUsername());
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", SenzUtil.getUid(context, cheque.getTimestamp().toString()));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz redeemSenz(Context context, Cheque cheque, String account) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("amnt", Integer.toString(cheque.getAmount()));
        senzAttributes.put("bnk", "sampath.chain");
        senzAttributes.put("acc", account);
        senzAttributes.put("blob", "");
        senzAttributes.put("id", cheque.getCid());
        senzAttributes.put("to", SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", SenzUtil.getUid(context, cheque.getTimestamp().toString()));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzService.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz senzFromPromize(Context context, Cheque cheque) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();

        // TODO set new timestamp and uid
        // TODO update them in db
        //Long timestamp = (System.currentTimeMillis() / 1000);
        //String uid = SenzUtil.getUid(context, timestamp.toString());
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", cheque.getUid());
        senzAttributes.put("user", cheque.getUser().getUsername());
        if (cheque.getUser().getSessionKey() != null && !cheque.getUser().getSessionKey().isEmpty()) {
            senzAttributes.put("$msg", CryptoUtil.encryptECB(CryptoUtil.getSecretKey(cheque.getUser().getSessionKey()), cheque.getBlob()));
        } else {
            senzAttributes.put("msg", cheque.getBlob());
        }

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(cheque.getUser().getUsername());
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static String getUid(Context context, String timestamp) {
        String username = PreferenceUtil.getSenzieAddress(context);
        return username + timestamp;
    }

}
