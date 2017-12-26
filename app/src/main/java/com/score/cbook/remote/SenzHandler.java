package com.score.cbook.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Secret;
import com.score.cbook.utils.CryptoUtils;
import com.score.cbook.utils.ImageUtils;
import com.score.cbook.utils.NotificationUtils;
import com.score.cbook.utils.PhoneBookUtil;
import com.score.cbook.utils.SenzParser;
import com.score.cbook.utils.SenzUtils;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.net.URLDecoder;

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
                case DATA:
                    Log.d(TAG, "DATA received");
                    handleData(senz, senzService);
                    break;
                case AWA:
                    Log.d(TAG, "AWA received");
                    ChequeSource.updateChequeDeliveryState(senzService.getApplicationContext(), senz.getAttributes().get("uid"), DeliveryState.RECEIVED);
                    SecretSource.updateSecretDeliveryState(senzService.getApplicationContext(), senz.getAttributes().get("uid"), DeliveryState.RECEIVED);
                    broadcastSenz(senz, senzService.getApplicationContext());
                    break;
                case GIYA:
                    Log.d(TAG, "GIYA received");
                    ChequeSource.updateChequeDeliveryState(senzService.getApplicationContext(), senz.getAttributes().get("uid"), DeliveryState.DELIVERED);
                    SecretSource.updateSecretDeliveryState(senzService.getApplicationContext(), senz.getAttributes().get("uid"), DeliveryState.DELIVERED);
                    broadcastSenz(senz, senzService.getApplicationContext());
                    break;
            }
        }
    }

    private void handleConnect(SenzService senzService) {
        // get all un-ack senzes from db
    }

    private void handleShare(Senz senz, SenzService senzService) {
        // first write AWA to switch
        senzService.writeSenz(SenzUtils.getAwaSenz(new User("", "senzswitch"), senz.getAttributes().get("uid")));

        if (senz.getAttributes().containsKey("msg") && senz.getAttributes().containsKey("status")) {
            try {
                // create user
                String username = senz.getSender().getUsername();
                ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
                if (chequeUser != null) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtils.decryptRSA(CryptoUtils.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    UserSource.updateUser(senzService.getApplicationContext(), username, "session_key", sessionKey);
                } else {
                    chequeUser = new ChequeUser(senz.getSender().getId(), senz.getSender().getUsername());
                    UserSource.createUser(senzService.getApplicationContext(), chequeUser);
                }

                // activate user
                UserSource.activateUser(senzService.getApplicationContext(), username);

                // notification user
                String notificationUser = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
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
            try {
                if (UserSource.isExistingUser(senzService.getApplicationContext(), senz.getSender().getUsername())) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtils.decryptRSA(CryptoUtils.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    UserSource.updateUser(senzService.getApplicationContext(), senz.getSender().getUsername(), "session_key", sessionKey);

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
        }
    }

    private void handleData(Senz senz, SenzService senzService) {
        if (senz.getAttributes().containsKey("status")) {
            String status = senz.getAttributes().get("status");
            if (status.equalsIgnoreCase("USER_SHARED")) {
                // user added successfully
                // save user in db
                String username = senz.getSender().getUsername();
                ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
                if (chequeUser != null) {
                    // existing user, activate it
                    UserSource.activateUser(senzService.getApplicationContext(), senz.getSender().getUsername());
                } else {
                    // not existing user
                    // this is when sharing directly by username
                    // create and activate uer
                    chequeUser = new ChequeUser("id", senz.getSender().getUsername());
                    UserSource.createUser(senzService.getApplicationContext(), chequeUser);
                    UserSource.activateUser(senzService.getApplicationContext(), chequeUser.getUsername());
                }

                // notification user
                String notificationUser = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
                SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(NotificationUtils.getUserConfirmNotification(notificationUser));
            }

            broadcastSenz(senz, senzService.getApplicationContext());
        } else if (senz.getAttributes().containsKey("msg") || senz.getAttributes().containsKey("$msg")) {
            // rahasa
            // send AWA back
            senzService.writeSenz(SenzUtils.getAwaSenz(new User("", "senzswitch"), senz.getAttributes().get("uid")));

            try {
                // save and broadcast
                String rahasa;
                if (senz.getAttributes().containsKey("$msg")) {
                    // encrypted data -> decrypt
                    String sessionKey = UserSource.getUser(senzService.getApplicationContext(), senz.getSender().getUsername()).getSessionKey();
                    rahasa = CryptoUtils.decryptECB(CryptoUtils.getSecretKey(sessionKey), senz.getAttributes().get("$msg"));
                } else {
                    // plain data
                    rahasa = URLDecoder.decode(senz.getAttributes().get("msg"), "UTF-8");
                }

                Long timestamp = (System.currentTimeMillis() / 1000);
                saveSecret(timestamp, senz.getAttributes().get("uid"), rahasa, BlobType.TEXT, senz.getSender(), senzService.getApplicationContext());
                senz.getAttributes().put("time", timestamp.toString());
                senz.getAttributes().put("msg", rahasa);
                broadcastSenz(senz, senzService.getApplicationContext());

                // notification user
                String username = senz.getSender().getUsername();
                ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
                String notificationUser = chequeUser.getUsername();
                if (chequeUser.getPhone() != null && !chequeUser.getPhone().isEmpty()) {
                    notificationUser = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
                }

                // show notification
                SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(
                        NotificationUtils.getSecretNotification(notificationUser, username, "New message received"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (senz.getAttributes().containsKey("pubkey")) {
            // pubkey from switch
            String username = senz.getAttributes().get("name");
            String pubKey = senz.getAttributes().get("pubkey");

            // update pubkey on db
            UserSource.updateUser(senzService.getApplicationContext(), username, "pubkey", pubKey);

            // Check if this user is the requester
            ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
            if (chequeUser.isSMSRequester()) {
                try {
                    // create session key for this user
                    String sessionKey = CryptoUtils.getSessionKey();
                    UserSource.updateUser(senzService.getApplicationContext(), username, "session_key", sessionKey);

                    String encryptedSessionKey = CryptoUtils.encryptRSA(CryptoUtils.getPublicKey(pubKey), sessionKey);
                    senzService.writeSenz(SenzUtils.getShareSenz(senzService.getApplicationContext(), username, encryptedSessionKey));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (senz.getAttributes().containsKey("cimg")) {
            // rahasa
            // send AWA back
            senzService.writeSenz(SenzUtils.getAwaSenz(new User("", "senzswitch"), senz.getAttributes().get("uid")));

            // save cheque
            Long timestamp = (System.currentTimeMillis() / 1000);
            User user = new User("id", senz.getAttributes().get("from"));
            int amnt = Integer.parseInt(senz.getAttributes().get("camnt"));
            String date = senz.getAttributes().get("cdate");
            saveCheque(timestamp, senz.getAttributes().get("uid"), senz.getAttributes().get("cid"), "", amnt, date, user, senzService.getApplicationContext());

            // save img
            String imgName = senz.getAttributes().get("uid") + ".jpg";
            ImageUtils.saveImg(imgName, senz.getAttributes().get("cimg"));

            // broadcast
            // notification user
            broadcastSenz(senz, senzService.getApplicationContext());
            ChequeUser secretUser = UserSource.getUser(senzService.getApplicationContext(), user.getUsername());

            // show notification
            String notificationUser = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
            SenzNotificationManager.getInstance(senzService.getApplicationContext()).showNotification(
                    NotificationUtils.getChequeNotification(notificationUser, "New cheque received", user.getUsername()));
        }
    }

    private void saveCheque(Long timestamp, String uid, String cid, String blob, int amnt, String date, User user, final Context context) {
        try {
            // create secret
            final Cheque cheque = new Cheque();
            cheque.setUid(uid);
            cheque.setTimestamp(timestamp);
            cheque.setDeliveryState(DeliveryState.NONE);
            cheque.setBlob(blob);
            cheque.setMyCheque(false);
            cheque.setCid(cid);
            cheque.setState("TRANSFER");
            cheque.setAmount(amnt);
            cheque.setDate(date);
            cheque.setViewed(false);

            ChequeUser chequeUser = new ChequeUser(user.getId(), user.getUsername());
            cheque.setUser(chequeUser);
            ChequeSource.createCheque(context, cheque);

            // update unread count by one
            UserSource.updateUnreadChequeCount(context, user.getUsername(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSecret(Long timestamp, String uid, String blob, BlobType blobType, User user, final Context context) {
        try {
            // create secret
            final Secret secret = new Secret();
            secret.setId(uid);
            secret.setTimeStamp(timestamp);
            secret.setDeliveryState(DeliveryState.NONE);
            secret.setBlob(blob);
            secret.setBlobType(blobType);
            secret.setMySecret(false);
            secret.setViewed(false);

            ChequeUser chequeUser = new ChequeUser(user.getId(), user.getUsername());
            secret.setUser(chequeUser);
            SecretSource.createSecret(context, secret);

            // update unread count by one
            UserSource.updateUnreadSecretCount(context, user.getUsername(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastSenz(Senz senz, Context context) {
        Intent intent = new Intent("com.score.cbook.SENZ");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);
    }

}
