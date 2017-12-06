package com.sinnovations.mediaguide.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Patil on 06-Dec-17.
 *
 */

public class NetworkUtils {

    private Context context;
    boolean isConnected = false;

    public NetworkUtils(Context context) {
        this.context = context;
    }

    public boolean getNetworkStatus() {
        if(context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return isConnected;
    }
}