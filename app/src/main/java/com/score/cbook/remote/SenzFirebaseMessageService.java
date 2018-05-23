package com.score.cbook.remote;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.score.cbook.R;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Notifcationz;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.SenzParser;
import com.score.senzc.pojos.Senz;

public class SenzFirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = SenzFirebaseMessageService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData());
            try {
                String msg = remoteMessage.getData().get("senz");
                Senz senz = SenzParser.parse(msg);
                handleSenz(senz);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleSenz(Senz senz) {
        if (senz.getAttributes().containsKey("pubkey")) {
            // connect
            addContact(senz);
        } else if (senz.getAttributes().containsKey("amnt")) {
            // promize
            addPromize(senz);
        }
    }

    private void addContact(Senz senz) {
        String phoneNo = senz.getAttributes().get("from");
        String contactName = PhoneBookUtil.getContactName(this, phoneNo);

        ChequeUser existingUser = UserSource.getExistingUserWithPhoneNo(this, phoneNo);
        if (existingUser == null) {
            // this means new connect request
            // create user
            ChequeUser chequeUser = new ChequeUser(phoneNo);
            chequeUser.setPhone(phoneNo);
            chequeUser.setPubKey(senz.getAttributes().get("pubkey"));
            chequeUser.setSMSRequester(false);
            chequeUser.setActive(false);
            UserSource.createUser(this, chequeUser);

            // notify
            Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, contactName, "New request received", phoneNo);
            NotificationzHandler.notifiyStatus(this, notifcationz);
        } else {
            // this means confirm connect request
            UserSource.activateUser(this, existingUser.getUsername());

            // notify
            Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, contactName, "Confirmed your request", phoneNo);
            NotificationzHandler.notifiyStatus(this, notifcationz);
        }
    }

    private void addPromize(Senz senz) {
        final Cheque cheque = new Cheque();
        Long timestamp = (System.currentTimeMillis() / 1000);
        cheque.setTimestamp(timestamp);
        cheque.setUid(senz.getAttributes().get("uid"));
        cheque.setDeliveryState(DeliveryState.NONE);
        cheque.setBlob("");
        cheque.setMyCheque(false);
        cheque.setCid(senz.getAttributes().get("id"));
        cheque.setChequeState(ChequeState.TRANSFER);
        cheque.setAmount(senz.getAttributes().get("amnt"));
        cheque.setViewed(false);

        String phoneNo = senz.getAttributes().get("from");
        ChequeUser chequeUser = new ChequeUser(phoneNo);
        cheque.setUser(chequeUser);
        ChequeSource.createCheque(this, cheque);

        // update unread count by one
        UserSource.updateUnreadChequeCount(this, phoneNo, 1);

        // notify
        String title = PhoneBookUtil.getContactName(this, phoneNo);
        Notifcationz notifcationz = new Notifcationz(R.drawable.ic_notification, title, "New iGift received", phoneNo);
        NotificationzHandler.notifyCheque(this, notifcationz);
    }

}

