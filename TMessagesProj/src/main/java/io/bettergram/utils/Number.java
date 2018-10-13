package io.bettergram.utils;

import android.annotation.SuppressLint;

public class Number {
    private static final long MILLION = 1000000L;
    private static final long BILLION = 1000000000L;
    private static final long TRILLION = 1000000000000L;

    @SuppressLint("DefaultLocale")
    public static String truncateNumber(double x) {
        return x < MILLION ? String.format("%.2f", x) :
                x < BILLION ? String.format("%.2f ", (x / MILLION)) + "M" :
                        x < TRILLION ? String.format("%.2f ", (x / BILLION)) + "B" :
                                String.format("%.2f ", (x / TRILLION)) + "T";
    }
}