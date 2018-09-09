package ru.johnlife.lifetools.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yan Yurkin on 6/2/2016.
 */
public class Restart {

    public static void app(Context context) {
        scheduleRestart(context);
        kill();
    }

    private static void scheduleRestart(Context context) {
        String name = context.getPackageName();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(name);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, name.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    }

    private static void kill() {
        //double kill
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

}
