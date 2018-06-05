package com.score.cbook.util;


import android.content.Context;
import android.telephony.SmsManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsUtil {
    public static void iGiftRequest(String phone) {
        String msg = "Please install sampath iGift app to happy share gifts";
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }

    public static void sendRequest(Context context, String phone) {
        String address = PreferenceUtil.get(context, PreferenceUtil.Z_ADDRESS);
        String msg = "#iGift #request\nI'm using sampath bank iGift app, #username " + address + " #code 41r33";
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }

    public static void sendAccept(Context context, String phone) {
        String address = PreferenceUtil.get(context, PreferenceUtil.Z_ADDRESS);
        String msg = "#iGift #confirm\nI have confirmed your request. #username " + address + " #code 31e3e";
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }

    public static String getUsernameFromSms(String smsMessage) {
        final Pattern pattern = Pattern.compile("#username\\s(\\S*)\\s");
        final Matcher matcher = pattern.matcher(smsMessage);
        matcher.find();
        return matcher.group(1);
    }

    public static String getKeyHashFromSms(String smsMessage) {
        final Pattern pattern = Pattern.compile("#code\\s(.*)$");
        final Matcher matcher = pattern.matcher(smsMessage);
        matcher.find();
        return matcher.group(1);
    }

    public static boolean isIgift(String smsMessage) {
        return smsMessage.toLowerCase().contains("#igift");
    }

    public static boolean isConfirm(String smsMessage) {
        return smsMessage.toLowerCase().contains("#confirm");
    }

    public static boolean isRequest(String smsMessage) {
        return smsMessage.toLowerCase().contains("#request");
    }
}
