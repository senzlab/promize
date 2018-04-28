package com.score.cbook.util;

import android.content.Context;

import com.score.cbook.pojo.Account;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.Secret;
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

    public static final String SWITCH_NAME = "senzswitch";
    public static final String SAMPATH_CHAIN_SENZIE_NAME = "sampath";
    public static final String SAMPATH_SUPPORT_SENZIE_NAME = "sampath.support";

    public static Senz regSenz(Context context, String sender) {
        // create create senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        Long timestamp = System.currentTimeMillis();
        String uid = timestamp.toString() + sender;
        senzAttributes.put("time", timestamp.toString());
        senzAttributes.put("uid", uid);
        senzAttributes.put("pubkey", PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY_NAME));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setSender(sender);
        senz.setReceiver(SenzUtil.SWITCH_NAME);
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
        senzAttributes.put("pubkey", PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY_NAME));

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setSender(sender);
        senz.setReceiver(SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
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
        senz.setReceiver(SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
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
        senz.setReceiver(SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
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
        senz.setReceiver(SenzUtil.SWITCH_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz shareSenz(Context context, String receiver, String sessionKey) throws NoSuchAlgorithmException {
        // create senz attributes
        Long timestamp = System.currentTimeMillis();
        HashMap<String, String> senzAttributes = new HashMap<>();
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
        senz.setReceiver(SenzUtil.SWITCH_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz transferSenz(Context context, Cheque cheque, Account account) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("amnt", cheque.getAmount());
        senzAttributes.put("bnk", account.getBank());
        senzAttributes.put("id", CryptoUtil.uuid());
        senzAttributes.put("acc", account.getAccountNo());
        senzAttributes.put("blob", cheque.getBlob());
        senzAttributes.put("to", cheque.getUser().getUsername());
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", SenzUtil.getUid(context, cheque.getTimestamp().toString()));
        senzAttributes.put("type", "TRANSFER");

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz redeemSenz(Context context, Cheque cheque, String bank, String account) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("amnt", cheque.getAmount());
        senzAttributes.put("bnk", bank);
        senzAttributes.put("id", cheque.getCid());
        senzAttributes.put("acc", account);
        senzAttributes.put("blob", "");
        senzAttributes.put("to", SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
        senzAttributes.put("time", cheque.getTimestamp().toString());
        senzAttributes.put("uid", SenzUtil.getUid(context, cheque.getTimestamp().toString()));
        senzAttributes.put("type", "REDEEM");

        // new senz
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.SHARE);
        senz.setReceiver(SenzUtil.SAMPATH_CHAIN_SENZIE_NAME);
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static Senz senzFromSecret(Secret secret) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", secret.getTimeStamp().toString());
        senzAttributes.put("uid", secret.getId());
        senzAttributes.put("user", secret.getUser().getUsername());
        if (secret.getUser().getSessionKey() != null && !secret.getUser().getSessionKey().isEmpty()) {
            senzAttributes.put("$msg", CryptoUtil.encryptECB(CryptoUtil.getSecretKey(secret.getUser().getSessionKey()), secret.getBlob()));
        } else {
            senzAttributes.put("msg", secret.getBlob());
        }

        // new senz object
        Senz senz = new Senz();
        senz.setSenzType(SenzTypeEnum.DATA);
        senz.setReceiver(secret.getUser().getUsername());
        senz.setAttributes(senzAttributes);

        return senz;
    }

    public static String getUid(Context context, String timestamp) {
        String username = PreferenceUtil.getSenzieAddress(context);
        return username + timestamp;
    }

}
