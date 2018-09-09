package ru.johnlife.lifetools.tools;

/**
 * Created by Yan Yurkin on 5/30/2016.
 */
public class Timestamp {
    public static int toSparseKey(long timestamp) {
        return (int) ( (timestamp/1000)&Integer.MAX_VALUE );
    }
}
