package com.github.cris16228.libcore;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.annotation.RequiresPermission;

public class NetworkUtils {

    public Dialog no_internet;
    Context context;

    public static NetworkUtils with(Context _context) {
        NetworkUtils networkUtils = new NetworkUtils();
        networkUtils.context = _context;
        return networkUtils;
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}
