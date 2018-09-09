package ru.johnlife.lifetools.tools;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by yanyu on 4/26/2016.
 */
public class DateUtil {
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Calendar tmp = Calendar.getInstance();

    public static synchronized long getBeginOfTheDay(long timestamp) {
//        tmp.setTimeZone(GMT);
        tmp.setTimeInMillis(timestamp);
        tmp.set(Calendar.HOUR_OF_DAY, 0);
        tmp.set(Calendar.MINUTE, 0);
        tmp.set(Calendar.SECOND, 0);
        tmp.set(Calendar.MILLISECOND,1);
        return tmp.getTimeInMillis();
    }

    public static synchronized long getBeginOfTheDay() {
        return getBeginOfTheDay(System.currentTimeMillis());
    }
}
