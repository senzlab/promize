package com.score.rahasak.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.DeliveryState;
import com.score.rahasak.pojo.Cheque;
import com.score.rahasak.pojo.SecretUser;
import com.score.rahasak.utils.CryptoUtils;
import com.score.rahasak.utils.ImageUtils;
import com.score.rahasak.utils.NotificationUtils;
import com.score.rahasak.utils.PhoneBookUtil;
import com.score.rahasak.utils.SenzParser;
import com.score.rahasak.utils.SenzUtils;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.ArrayList;
import java.util.List;

class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();

    private static SenzHandler instance;

    static SenzHandler getInstance() {
        if (instance == null) {
            instance = new SenzHandler();
        }

        return instance;
    }

    void handle(String senzMsg, SenzService senzService) {
        if (senzMsg.equalsIgnoreCase("TAK")) {
            // senz service connected, send un-ack senzes if available
            handleConnect(senzService);
        } else if (senzMsg.equalsIgnoreCase("TIK")) {
            // write tuk from here
            senzService.write("TUK");
        } else {
            // actual senz received
            Senz senz = SenzParser.parse(senzMsg);
            switch (senz.getSenzType()) {
                case SHARE:
                    Log.d(TAG, "SHARE received");
                    handleShare(senz, senzService);
                    break;
                case GET:
                    Log.d(TAG, "GET received");
                    handleGet(senz, senzService);
                    break;
                case DATA:
                    Log.d(TAG, "DATA received");
                    handleData(senz, senzService);
                    break;
                case STREAM:
                    Log.d(TAG, "STREAM received");
                    handleStream(senz, senzService);
                    break;
            }
        }
    }

    private void handleConnect(SenzService senzService) {
        // get all un-ack senzes from db
        List<Senz> unackSenzes = new ArrayList<>();
        for (Cheque cheque : new SenzorsDbSource(senzService.getApplicationContext()).getUnAckSecrects()) {
            try {
                unackSenzes.add(SenzUtils.getSenzFromCheque(senzService.getApplicationContext(), cheque));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // send them
        senzService.writeSenzes(unackSenzes);
    }

    private void handleShare(Senz senz, SenzService senzService) {
        if (senz.getAttributes().containsKey("msg") && senz.getAttributes().containsKey("status")) {
            // new user
            // new user permissions, save to db
            SenzorsDbSource dbSource = new SenzorsDbSource(senzService.getApplicationContext());
            try {
                // create user
                String username = senz.getSender().getUsername();
                SecretUser secretUser = dbSource.getSecretUser(username);
                if (secretUser != null) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtils.decryptRSA(CryptoUtils.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    dbSource.updateSecretUser(username, "session_key", sessionKey);
                } else {
                    secretUser = new SecretUser(senz.getSender().getId(), senz.getSender().getUsername());
                    dbSource.createSecretUser(secretUser);
                }

                // activate user
                dbSource.activateSecretUser(username, true);

                // notification user
                String notificationUser = username;
                if (secretUser.getPhone() != null && !secretUser.getPhone().isEmpty()) {
                    notificationUser = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
                }
                SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(NotificationUtils.getUserNotification(notificationUser));

                // broadcast send status back
                broadcastSenz(senz, senzService.getApplicationContext());
                senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "USER_SHARED"));
            } catch (Exception ex) {
                ex.printStackTrace();

                // send error ack
                senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "USER_SHARE_FAILED"));
            }
        } else if (senz.getAttributes().containsKey("$skey")) {
            // re sharing session key
            // broadcast send status back
            SenzorsDbSource dbSource = new SenzorsDbSource(senzService.getApplicationContext());
            try {
                if (dbSource.isExistingUser(senz.getSender().getUsername())) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtils.decryptRSA(CryptoUtils.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    dbSource.updateSecretUser(senz.getSender().getUsername(), "session_key", sessionKey);

                    broadcastSenz(senz, senzService.getApplicationContext());
                    senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "KEY_SHARED"));
                } else {
                    // means error
                    senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "KEY_SHARE_FAILED"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                // send error ack
                senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "KEY_SHARE_FAILED"));
            }
        } else if (senz.getAttributes().containsKey("cimg")) {
            // new cheque most probably
            // send status back first
            senzService.writeSenz(SenzUtils.getAckSenz(senz.getSender(), senz.getAttributes().get("uid"), "DELIVERED"));

            // save and broadcast
            Long timestamp = (System.currentTimeMillis() / 1000);
            User user = new User("id", senz.getAttributes().get("from"));
            saveSecret(timestamp, senz.getAttributes().get("uid"), "", user, senzService.getApplicationContext());
            String imgName = senz.getAttributes().get("uid") + ".jpg";
            ImageUtils.saveImg(imgName, senz.getAttributes().get("cimg"));
            broadcastSenz(senz, senzService.getApplicationContext());

            // notification user
            String username = senz.getSender().getUsername();
//            SecretUser secretUser = new SenzorsDbSource(senzService.getApplicationContext()).getSecretUser(username);
//            String notificationUser = secretUser.getUsername();
//            if (secretUser.getPhone() != null && !secretUser.getPhone().isEmpty()) {
//                notificationUser = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
//            }

            // show notification
            SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(
                    NotificationUtils.getStreamNotification(username, "New cheque received", username));
        }
    }

    private void handleGet(Senz senz, SenzService senzService) {

    }

    private void handleData(Senz senz, SenzService senzService) {
        // save in db
        SenzorsDbSource dbSource = new SenzorsDbSource(senzService.getApplicationContext());

        if (senz.getAttributes().containsKey("status")) {
            // status coming from switch
            // broadcast
            updateStatus(senz, senzService.getApplicationContext());

            String status = senz.getAttributes().get("status");
            if (status.equalsIgnoreCase("USER_SHARED")) {
                // user added successfully
                // save user in db
                String username = senz.getSender().getUsername();
                SecretUser secretUser = dbSource.getSecretUser(username);
                if (secretUser != null) {
                    // existing user, activate it
                    dbSource.activateSecretUser(senz.getSender().getUsername(), true);
                } else {
                    // not existing user
                    // this is when sharing directly by username
                    // create and activate uer
                    secretUser = new SecretUser("id", senz.getSender().getUsername());
                    dbSource.createSecretUser(secretUser);
                    dbSource.activateSecretUser(secretUser.getUsername(), true);
                }

                // notification user
                String notificationUser = username;
                if (secretUser.getPhone() != null && !secretUser.getPhone().isEmpty()) {
                    notificationUser = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
                }
                SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(NotificationUtils.getUserConfirmNotification(notificationUser));
            }

            broadcastSenz(senz, senzService.getApplicationContext());
        } else if (senz.getAttributes().containsKey("pubkey")) {
            // pubkey from switch
            String username = senz.getAttributes().get("name");
            String pubKey = senz.getAttributes().get("pubkey");

            // update pubkey on db
            dbSource.updateSecretUser(username, "pubkey", pubKey);

            // Check if this user is the requester
            SecretUser secretUser = dbSource.getSecretUser(username);
            if (secretUser.isSMSRequester()) {
                try {
                    // create session key for this user
                    String sessionKey = CryptoUtils.getSessionKey();
                    dbSource.updateSecretUser(username, "session_key", sessionKey);

                    String encryptedSessionKey = CryptoUtils.encryptRSA(CryptoUtils.getPublicKey(pubKey), sessionKey);
                    senzService.writeSenz(SenzUtils.getShareSenz(senzService.getApplicationContext(), username, encryptedSessionKey));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (senz.getAttributes().containsKey("mic")) {
            broadcastSenz(senz, senzService.getApplicationContext());
        } else if (senz.getAttributes().containsKey("senz")) {
            String senzMsg = new String(Base64.decode(senz.getAttributes().get("senz"), Base64.DEFAULT));
            Senz innerSenz = SenzParser.parse(senzMsg);

            senzService.writeSenz(SenzUtils.getAckSenz(new User("", "senzswitch"), innerSenz.getAttributes().get("uid"), "DELIVERED"));
            if (innerSenz.getAttributes().containsKey("cam")) {
                // selfie mis
                Long timestamp = (System.currentTimeMillis() / 1000);
                saveSecret(timestamp, innerSenz.getAttributes().get("uid"), "", innerSenz.getSender(), senzService.getApplicationContext());

                // notification user
                String username = innerSenz.getSender().getUsername();
                SecretUser secretUser = dbSource.getSecretUser(username);
                String notificationUser = secretUser.getUsername();
                if (secretUser.getPhone() != null && !secretUser.getPhone().isEmpty()) {
                    notificationUser = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
                }

                // show notification
                SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(
                        NotificationUtils.getStreamNotification(notificationUser, "Missed selfie call", username));
            }
        }
    }

    private void handleStream(Senz senz, SenzService senzService) {
        // stream
    }

    private void broadcastSenz(Senz senz, Context context) {
        Intent intent = new Intent("com.score.rahasak.SENZ");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);
    }

    private void saveSecret(Long timestamp, String uid, String blob, User user, final Context context) {
        try {
            // create secret
            final Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setTimestamp(timestamp);
            cheque.setDeliveryState(DeliveryState.NONE);
            cheque.setBlob(blob);
            new SenzorsDbSource(context).createSecret(cheque);

            // update unread count by one
            new SenzorsDbSource(context).updateUnreadSecretCount(user.getUsername(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(Senz senz, final Context context) {
        try {
            final String uid = senz.getAttributes().get("uid");
            String status = senz.getAttributes().get("status");
            if (status.equalsIgnoreCase("DELIVERED")) {
                new SenzorsDbSource(context).updateDeliveryStatus(DeliveryState.DELIVERED, uid);
            } else if (status.equalsIgnoreCase("RECEIVED")) {
                new SenzorsDbSource(context).updateDeliveryStatus(DeliveryState.RECEIVED, uid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
