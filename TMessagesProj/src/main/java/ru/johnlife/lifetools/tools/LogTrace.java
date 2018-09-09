package ru.johnlife.lifetools.tools;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by yanyu on 4/23/2016.
 */
public class LogTrace {
    public static void d(String tag, int lastN) {
        Log.d(tag, build(lastN));
    }

    public static void i(String tag, int lastN) {
        Log.i(tag, build(lastN));
    }

    public static void w(String tag, int lastN) {
        Log.w(tag, build(lastN));
    }

    @NonNull
    private static String build(int lastN) {
        try {
            throw new Exception();
        } catch (Exception e) {
            int dx = 3;
            StackTraceElement[] trace = e.getStackTrace();
            StringBuilder b = new StringBuilder();
            for (int i=dx; i<lastN+dx; i++) {
                StackTraceElement entry = trace[i];
                String s = entry.toString();
                if (s.contains(".access$")) {
                    dx++;
                    continue;
                }
                b.append(" - ").append(entry).append('\n');
            }
            return b.toString();
        }

    }
}
