package com.score.cbook.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.score.cbook.remote.SenzService;
import com.score.cbook.utils.NetworkUtil;

public class NetworkStatusReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkStatusReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network status changed");

        if (NetworkUtil.isAvailableNetwork(context)) {
            context.startService(new Intent(context, SenzService.class));
        } else {
            Log.e(TAG, "No network to start senz service");
        }
    }
}
