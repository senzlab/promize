package com.score.cbook.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.SenzorsDbSource;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.remote.SenzNotificationManager;
import com.score.cbook.utils.NotificationUtils;
import com.score.cbook.utils.PhoneBookUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage smsMessage;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = bundle.getString("format");
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0], format);
                    } else {
                        // this method was deprecated in API level 23.
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
                    }

                    if (isMessageFromRahasakApp(smsMessage.getMessageBody())) {
                        // valid message
                        if (isMessageConfirm(smsMessage.getMessageBody())) {
                            initFriendConfirmation(smsMessage, context);
                        } else if (isMessageRequest(smsMessage.getMessageBody())) {
                            initFriendRequest(smsMessage, context);
                        }
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private boolean isMessageFromRahasakApp(String smsMessage) {
        return smsMessage.toLowerCase().contains("#rahasak");
    }

    private boolean isMessageConfirm(String smsMessage) {
        return smsMessage.toLowerCase().contains("#confirm");
    }

    private boolean isMessageRequest(String smsMessage) {
        return smsMessage.toLowerCase().contains("#request");
    }

    private String getUsernameFromSms(String smsMessage) {
        final Pattern pattern = Pattern.compile("#username\\s(\\S*)\\s");
        final Matcher matcher = pattern.matcher(smsMessage);
        matcher.find();
        return matcher.group(1);
    }

    private String getKeyHashFromSms(String smsMessage) {
        final Pattern pattern = Pattern.compile("#code\\s(.*)$");
        final Matcher matcher = pattern.matcher(smsMessage);
        matcher.find();
        return matcher.group(1);
    }

    private void initFriendRequest(SmsMessage smsMessage, Context context) {
        String contactNo = smsMessage.getOriginatingAddress();
        String contactName = PhoneBookUtil.getContactName(context, contactNo);
        String username = getUsernameFromSms(smsMessage.getMessageBody());
        String pubKeyHash = getKeyHashFromSms(smsMessage.getMessageBody());

        SenzorsDbSource dbSource = new SenzorsDbSource(context);

        // delete existing user
        ChequeUser existingUser = dbSource.getExistingUserWithPhoneNo(contactNo);
        if (existingUser != null) {
            dbSource.deleteSecretUser(existingUser.getUsername());
        }

        // create user
        ChequeUser chequeUser = new ChequeUser("id", username);
        chequeUser.setPhone(contactNo);
        chequeUser.setPubKeyHash(pubKeyHash);
        dbSource.createSecretUser(chequeUser);

        // show Notification
        SenzNotificationManager.getInstance(context.getApplicationContext()).showNotification(NotificationUtils.getSmsNotification(contactName, contactNo, username));
    }

    private void initFriendConfirmation(SmsMessage smsMessage, Context context) {
        String contactNo = smsMessage.getOriginatingAddress();
        String username = getUsernameFromSms(smsMessage.getMessageBody());
        String pubKeyHash = getKeyHashFromSms(smsMessage.getMessageBody());

        try {
            // create user
            SenzorsDbSource dbSource = new SenzorsDbSource(context);
            ChequeUser chequeUser = new ChequeUser("id", username);
            chequeUser.setPhone(contactNo);
            chequeUser.setPubKeyHash(pubKeyHash);
            chequeUser.setSMSRequester(true);
            if (!dbSource.isExistingUserWithPhoneNo(contactNo)) {
                dbSource.createSecretUser(chequeUser);
            }

            // broadcast
            Intent intent = new Intent();
            intent.setAction(IntentProvider.ACTION_SMS_REQUEST_CONFIRM);
            intent.putExtra("USERNAME", username);
            intent.putExtra("PHONE", contactNo);
            context.sendBroadcast(intent);
        } catch (Exception ex) {
            // user exists
            ex.printStackTrace();
        }
    }
}