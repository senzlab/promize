package com.score.cbook.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.UserSource;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Notifcationz;
import com.score.cbook.remote.NotificationzHandler;
import com.score.cbook.util.PhoneBookUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmsReceiver extends BroadcastReceiver {
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

                    if (isMessageFromSenz(smsMessage.getMessageBody())) {
                        if (isRequestConfirm(smsMessage.getMessageBody())) {
                            initFriendConfirmation(smsMessage, context);
                        } else if (isRequest(smsMessage.getMessageBody())) {
                            initFriendRequest(smsMessage, context);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private boolean isMessageFromSenz(String smsMessage) {
        return smsMessage.toLowerCase().contains("#chequebook");
    }

    private boolean isRequestConfirm(String smsMessage) {
        return smsMessage.toLowerCase().contains("#confirm");
    }

    private boolean isRequest(String smsMessage) {
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

        // delete existing user
        ChequeUser existingUser = UserSource.getExistingUserWithPhoneNo(context, contactNo);
        if (existingUser != null) {
            UserSource.deleteUser(context, existingUser.getUsername());
        }

        // create user
        ChequeUser chequeUser = new ChequeUser(username);
        chequeUser.setPhone(contactNo);
        chequeUser.setPubKeyHash(pubKeyHash);
        UserSource.createUser(context, chequeUser);

        // notify
        String msg = "Would you to add " + contactName + " as cheque book customer?";
        Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, contactName, msg, username);
        notifcationz.setSenderPhone(contactNo);
        notifcationz.setAddActions(true);
        NotificationzHandler.notifiyCustomer(context, notifcationz);
    }

    private void initFriendConfirmation(SmsMessage smsMessage, Context context) {
        String contactNo = smsMessage.getOriginatingAddress();
        String username = getUsernameFromSms(smsMessage.getMessageBody());
        String pubKeyHash = getKeyHashFromSms(smsMessage.getMessageBody());

        try {
            // create user
            ChequeUser chequeUser = new ChequeUser(username);
            chequeUser.setPhone(contactNo);
            chequeUser.setPubKeyHash(pubKeyHash);
            chequeUser.setSMSRequester(true);
            if (!UserSource.isExistingUserWithPhoneNo(context, contactNo)) {
                UserSource.createUser(context, chequeUser);
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