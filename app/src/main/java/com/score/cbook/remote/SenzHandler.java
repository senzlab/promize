package com.score.cbook.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.score.cbook.R;
import com.score.cbook.application.SenzApplication;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Notifcationz;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

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
            senzService.tukSenz();
        } else {
            // actual senz received
            Senz senz = SenzParser.parse(senzMsg);
            switch (senz.getSenzType()) {
                case SHARE:
                    Log.d(TAG, "SHARE received");
                    // first write AWA to switch
                    // then handle
                    senzService.writeSenz(SenzUtil.awaSenz(senz.getAttributes().get("uid")));
                    handleShare(senz, senzService);
                    break;
                case DATA:
                    Log.d(TAG, "DATA received");
                    // send AWA back
                    // then handle
                    if (!senz.getAttributes().containsValue("REG_DONE") && !senz.getAttributes().containsValue("REG_ALR")) {
                        senzService.writeSenz(SenzUtil.awaSenz(senz.getAttributes().get("uid")));
                    }

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
        if (senz.getAttributes().containsKey("msg") && senz.getAttributes().containsKey("status")) {
            try {
                // create user
                String username = senz.getSender();
                ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
                if (chequeUser != null) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtil.decryptRSA(CryptoUtil.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    UserSource.updateUser(senzService.getApplicationContext(), username, "session_key", sessionKey);
                } else {
                    chequeUser = new ChequeUser(senz.getSender());
                    UserSource.createUser(senzService.getApplicationContext(), chequeUser);
                }

                // activate user
                UserSource.activateUser(senzService.getApplicationContext(), username);

                // broadcast send status back
                broadcastSenz(senz, senzService.getApplicationContext());
                senzService.writeSenz(SenzUtil.statusSenz(senzService.getApplicationContext(), senz.getSender(), "USER_SHARED"));

                // notification
                String title = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
                Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, title, senzService.getString(R.string.customer_notification), username);
                NotificationzHandler.notifiyStatus(senzService, notifcationz);
            } catch (Exception ex) {
                ex.printStackTrace();

                // send error ack
                senzService.writeSenz(SenzUtil.statusSenz(senzService.getApplicationContext(), senz.getSender(), "USER_SHARE_FAILED"));
            }
        } else if (senz.getAttributes().containsKey("$skey")) {
            try {
                if (UserSource.isExistingUser(senzService.getApplicationContext(), senz.getSender())) {
                    String encryptedSessionKey = senz.getAttributes().get("$skey");
                    String sessionKey = CryptoUtil.decryptRSA(CryptoUtil.getPrivateKey(senzService.getApplicationContext()), encryptedSessionKey);
                    UserSource.updateUser(senzService.getApplicationContext(), senz.getSender(), "session_key", sessionKey);

                    broadcastSenz(senz, senzService.getApplicationContext());
                    senzService.writeSenz(SenzUtil.statusSenz(senzService.getApplicationContext(), senz.getSender(), "KEY_SHARED"));
                } else {
                    // means error
                    senzService.writeSenz(SenzUtil.statusSenz(senzService.getApplicationContext(), senz.getSender(), "KEY_SHARE_FAILED"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                // send error ack
                senzService.writeSenz(SenzUtil.statusSenz(senzService.getApplicationContext(), senz.getSender(), "KEY_SHARE_FAILED"));
            }
        } else if (senz.getAttributes().containsKey("blob")) {
            try {
                // save cheque
                Long timestamp = (System.currentTimeMillis() / 1000);
                String user = senz.getAttributes().get("from");
                saveCheque(timestamp, senz.getAttributes().get("uid"), senz.getAttributes().get("id"), senz.getAttributes().get("amnt"), user, senzService.getApplicationContext());

                // save img
                String imgName = senz.getAttributes().get("uid") + ".jpg";
                ImageUtil.saveImg(imgName, senz.getAttributes().get("blob"));

                // broadcast
                broadcastSenz(senz, senzService.getApplicationContext());
                ChequeUser secretUser = UserSource.getUser(senzService.getApplicationContext(), user);

                // show notification
                String title = PhoneBookUtil.getContactName(senzService, secretUser.getPhone());
                Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, title, senzService.getString(R.string.cheque_notification), user);
                NotificationzHandler.notifyCheque(senzService.getApplicationContext(), notifcationz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleData(Senz senz, SenzService senzService) {
        if (senz.getAttributes().containsKey("status")) {
            String status = senz.getAttributes().get("status");
            if (status.equalsIgnoreCase("USER_SHARED")) {
                // user added successfully
                // save user in db
                String username = senz.getSender();
                ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), username);
                if (chequeUser != null) {
                    // existing user, activate it
                    UserSource.activateUser(senzService.getApplicationContext(), senz.getSender());
                } else {
                    // not existing user
                    // this is when sharing directly by username
                    // create and activate uer
                    chequeUser = new ChequeUser(senz.getSender());
                    UserSource.createUser(senzService.getApplicationContext(), chequeUser);
                    UserSource.activateUser(senzService.getApplicationContext(), chequeUser.getUsername());
                }

                // notification user
                String title = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
                Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, title, senzService.getString(R.string.confirmed_notification), username);
                NotificationzHandler.notifiyStatus(senzService, notifcationz);
            }

            broadcastSenz(senz, senzService.getApplicationContext());
        } else if (senz.getAttributes().containsKey("msg") || senz.getAttributes().containsKey("$msg")) {
            // rahasa
            try {
                // save and broadcast
                String rahasa;
                if (senz.getAttributes().containsKey("$msg")) {
                    // encrypted data -> decrypt
                    String sessionKey = UserSource.getUser(senzService.getApplicationContext(), senz.getSender()).getSessionKey();
                    rahasa = CryptoUtil.decryptECB(CryptoUtil.getSecretKey(sessionKey), senz.getAttributes().get("$msg"));
                } else {
                    // plain data
                    rahasa = URLDecoder.decode(senz.getAttributes().get("msg"), "UTF-8");
                }

                Long timestamp = (System.currentTimeMillis() / 1000);
                saveSecret(timestamp, senz.getAttributes().get("uid"), rahasa, BlobType.TEXT, senz.getSender(), senzService.getApplicationContext());
                senz.getAttributes().put("time", timestamp.toString());
                senz.getAttributes().put("msg", rahasa);
                broadcastSenz(senz, senzService.getApplicationContext());

                // show notification when not in chat with senz sender
                if (!SenzApplication.isOnChat() || !SenzApplication.getChatUser().equalsIgnoreCase(senz.getSender())) {
                    ChequeUser chequeUser = UserSource.getUser(senzService.getApplicationContext(), senz.getSender());
                    String title = PhoneBookUtil.getContactName(senzService, chequeUser.getPhone());
                    Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, title, senzService.getString(R.string.message_notification), senz.getSender());
                    NotificationzHandler.notifyMessage(senzService.getApplicationContext(), notifcationz);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (senz.getAttributes().containsKey("pubkey")) {
            // pubkey from switch
            // update pubkey
            String username = senz.getAttributes().get("name");
            String pubKey = senz.getAttributes().get("pubkey");
            UserSource.updateUser(senzService.getApplicationContext(), username, "pubkey", pubKey);

            // Check if this user is the requester
            ChequeUser user = UserSource.getUser(senzService.getApplicationContext(), username);
            if (user != null && user.isSMSRequester()) {
                try {
                    // create session key for this user
                    String sessionKey = CryptoUtil.getSessionKey();
                    UserSource.updateUser(senzService.getApplicationContext(), username, "session_key", sessionKey);

                    String encryptedSessionKey = CryptoUtil.encryptRSA(CryptoUtil.getPublicKey(pubKey), sessionKey);
                    senzService.writeSenz(SenzUtil.shareSenz(senzService.getApplicationContext(), username, encryptedSessionKey));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveCheque(Long timestamp, String uid, String id, String amnt, String user, final Context context) {
        // create secret
        final Cheque cheque = new Cheque();
        cheque.setUid(uid);
        cheque.setTimestamp(timestamp);
        cheque.setDeliveryState(DeliveryState.NONE);
        cheque.setBlob("");
        cheque.setMyCheque(false);
        cheque.setCid(id);
        cheque.setChequeState(ChequeState.TRANSFER);
        cheque.setAmount(amnt);
        cheque.setViewed(false);

        ChequeUser chequeUser = new ChequeUser(user);
        cheque.setUser(chequeUser);
        ChequeSource.createCheque(context, cheque);

        // update unread count by one
        UserSource.updateUnreadChequeCount(context, user, 1);
    }

    private void saveSecret(Long timestamp, String uid, String blob, BlobType blobType, String user, final Context context) {
        // create secret
        final Secret secret = new Secret();
        secret.setId(uid);
        secret.setTimeStamp(timestamp);
        secret.setDeliveryState(DeliveryState.NONE);
        secret.setBlob(blob);
        secret.setBlobType(blobType);
        secret.setMySecret(false);
        secret.setViewed(false);

        ChequeUser chequeUser = new ChequeUser(user);
        secret.setUser(chequeUser);
        SecretSource.createSecret(context, secret);

        // update unread count by one
        UserSource.updateUnreadSecretCount(context, user, 1);
    }

    private void broadcastSenz(Senz senz, Context context) {
        Intent intent = new Intent("com.score.cbook.SENZ");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);
    }

}
