package com.ash.randomzy.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

public class NetworkUtil {

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (connectivityManager.getNetworkCapabilities(network).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                return  true;
        }else {
            return connectivityManager.getActiveNetworkInfo() != null;
        }
        return false;
    }
}
