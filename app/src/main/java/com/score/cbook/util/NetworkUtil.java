package com.score.cbook.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class deal with generic network and IO functionality
 *
 * @author eranga.herath@pagero.com (eranga herath)
 */
public class NetworkUtil {

    /**
     * Check network connection availability
     *
     * @param context need to access ConnectivityManager
     * @return availability of network
     */
    public static boolean isAvailableNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}