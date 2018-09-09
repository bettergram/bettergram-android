package ru.johnlife.lifetools.tools;

import android.location.Location;

public class Distance {
    public static float between(Location a, Location b) {
        return between(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    }

    public static float between(Location a, double latB, double lngB) {
        return between(a.getLatitude(), a.getLongitude(), latB, lngB);
    }

    public static float between(double lat1, double lng1, double lat2, double lng2) {
        float[] d = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, d);
        return d[0];
    }
}
