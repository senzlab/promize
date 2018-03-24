package com.score.cbook.util;


import android.content.Context;
import android.telephony.SmsManager;

public class SmsUtil {
    public static void sendRequest(Context context, String phone) {
        String address = PreferenceUtil.getSenzieAddress(context);
        String msg = "#ChequeBook #request\nI'm using sampath bank digital cheque book app, #username " + address + " #code 41r33";
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }

    public static void sendAccept(Context context, String phone) {
        String address = PreferenceUtil.getSenzieAddress(context);
        String msg = "#ChequeBook #confirm\nI have confirmed your request. #username " + address + " #code 31e3e";
        SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
    }
}
