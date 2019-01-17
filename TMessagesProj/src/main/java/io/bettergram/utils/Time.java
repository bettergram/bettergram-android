package io.bettergram.utils;

import java.util.Calendar;
import java.util.Date;

import io.bettergram.messenger.R;
import io.bettergram.telegram.messenger.LocaleController;

public class Time {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(Date date) {
        long time = date.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = currentDate().getTime();

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return LocaleController.getString("NewsMomentsAgo", R.string.NewsMomentsAgo);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return LocaleController.getString("NewsMinuteAgo", R.string.NewsMinuteAgo);
        } else if (diff < 60 * MINUTE_MILLIS) {
            return String.format(LocaleController.getString("NewsMinutesAgo", R.string.NewsMinutesAgo), (diff / MINUTE_MILLIS));
        } else if (diff < 2 * HOUR_MILLIS) {
            return LocaleController.getString("NewsAnHourAgo", R.string.NewsAnHourAgo);
        } else if (diff < 24 * HOUR_MILLIS) {
            return String.format(LocaleController.getString("NewsHoursAgo", R.string.NewsHoursAgo), (diff / HOUR_MILLIS));
        } else if (diff < 48 * HOUR_MILLIS) {
            return LocaleController.getString("NewsYesterday", R.string.NewsYesterday);
        } else {
            return String.format(LocaleController.getString("NewsDaysAgo", R.string.NewsDaysAgo), (diff / DAY_MILLIS));
        }
    }

    public static int currentMillis() {
        return (int) (System.currentTimeMillis() % 0x7fffffff);
    }
}
