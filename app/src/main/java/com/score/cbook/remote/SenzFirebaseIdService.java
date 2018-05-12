package com.score.cbook.remote;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.score.cbook.util.PreferenceUtil;


public class SenzFirebaseIdService extends FirebaseInstanceIdService {
    private static final String TAG = SenzFirebaseIdService.class.getName();

    @Override
    public void onTokenRefresh() {
        // get updated token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "refreshed token: " + refreshedToken);

        // save firebase token in shared prefs
        if (!PreferenceUtil.get(this, PreferenceUtil.FIREBASE_TOKEN).equalsIgnoreCase(refreshedToken)) {
            PreferenceUtil.put(this, PreferenceUtil.FIREBASE_TOKEN, refreshedToken);

            // todo update token in zwitch
        }
    }

}
