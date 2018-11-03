package io.bettergram.utils;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Counter {

    private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();

    static {
        SUFFIXES.put(1_000L, "k");
        SUFFIXES.put(1_000_000L, "M");
        SUFFIXES.put(1_000_000_000L, "G");
        SUFFIXES.put(1_000_000_000_000L, "T");
        SUFFIXES.put(1_000_000_000_000_000L, "P");
        SUFFIXES.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(String value) {
        return format(Long.valueOf(value));
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) {
            return format(Long.MIN_VALUE + 1);
        }
        if (value < 0) {
            return "-" + format(-value);
        }
        if (value < 1000) {
            return Long.toString(value); //deal with easy case
        }

        Entry<Long, String> e = SUFFIXES.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
