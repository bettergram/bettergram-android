package ru.johnlife.lifetools.essentials;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public interface Constants {
	String UTF_8 = "UTF-8";
	Locale RUSSIAN = new Locale("ru");
	Locale ENGLISH_US = Locale.US;
	
	long SECOND = 1000;
	long MINUTE = 60 * SECOND;
	long HOUR = 60 * MINUTE;
	long DAY = 24 * HOUR;
	long WEEK = 7 * DAY;
	long MONTH = 30 * DAY;
	long YEAR = 365 * DAY;
    int NO_VALUE = Integer.MIN_VALUE+4;
}
