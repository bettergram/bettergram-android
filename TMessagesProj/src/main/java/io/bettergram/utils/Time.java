package io.bettergram.utils;

public class Time {

    public static int currentMillis() {
        return (int) (System.currentTimeMillis() % 0x7fffffff);
    }
}
