package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Yan Yurkin
 * 15 June 2016
 */
public class Connection {
    private static Connection instance;

    public static Connection of(Context context) {
        if (null == instance) {
            instance = new Connection(context);
        }
        return instance;
    }

    private boolean hasWifi = false;
    private boolean hasMobile = false;

    private Connection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    hasWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    hasMobile = true;
        }
    }

    public boolean hasWifi() {
        return hasWifi;
    }
    public boolean hasMobile() {
        return hasMobile;
    }
}
