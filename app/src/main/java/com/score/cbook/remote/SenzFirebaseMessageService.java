package com.score.cbook.remote;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.pojo.ChequeUser;
import com.score.senzc.pojos.Senz;

import org.json.JSONObject;

public class SenzFirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = SenzFirebaseMessageService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().entrySet());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                //Senz senz = SenzParser.parse(json.getString("message"));
                //handleSenz(senz);
                Log.d(TAG, json.toString());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleSenz(Senz senz) {
        // save promize
        Long timestamp = (System.currentTimeMillis() / 1000);
        String user = senz.getAttributes().get("from");
        //savePromize(timestamp, senz.getAttributes().get("uid"), senz.getAttributes().get("id"), senz.getAttributes().get("amnt"), user);

        // show notification
    }

    private void saveUser(Senz senz) {

    }

    private void activeUser(Senz senz) {

    }

    private void savePromize(Long timestamp, String uid, String id, String amnt, String user) {
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
        ChequeSource.createCheque(this, cheque);

        // update unread count by one
        UserSource.updateUnreadChequeCount(this, user, 1);
    }

}
