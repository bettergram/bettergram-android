package io.bettergram;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.NotificationFactory;
import com.urbanairship.util.NotificationIdGenerator;
import io.bettergram.messenger.BuildConfig;
import io.bettergram.messenger.R;
import io.bettergram.utils.Assets;

import java.io.IOException;

public class BetterPilot extends Autopilot {

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(true);
        // Android O
        if (Build.VERSION.SDK_INT >= 26) {
            Context context = UAirship.getApplicationContext();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel("bettergram_channel", "bettergram_channel", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }
        final SystemNotificationFactory notificationFactory = new SystemNotificationFactory(UAirship.getApplicationContext());
        airship.getPushManager().setNotificationFactory(notificationFactory);
    }

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        try {
            Assets assets = new Assets(context).fromFile("airshipconfig.properties");
            return new AirshipConfigOptions.Builder()
                    .setDevelopmentAppKey(assets.getProperty("developmentAppKey"))
                    .setDevelopmentAppSecret(assets.getProperty("developmentAppSecret"))
                    .setProductionAppKey(assets.getProperty("productionAppKey"))
                    .setProductionAppSecret(assets.getProperty("productionAppSecret"))
                    .setInProduction(!BuildConfig.DEBUG)
                    .setGcmSender(assets.getProperty("fcmSenderId")) // FCM/GCM sender ID
                    .setNotificationIcon(R.drawable.notification)
                    //.setNotificationAccentColor(Theme.getColor(Theme.key_actionBarDefault))
                    .setNotificationChannel("bettergram_channel")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    class SystemNotificationFactory extends NotificationFactory {

        SystemNotificationFactory(Context context) {
            super(context);
        }

        @Override
        public Notification createNotification(PushMessage message, int notificationId) {
            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                    .setContentTitle(getContext().getString(R.string.AppName))
                    .setContentText(message.getAlert())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.notification);
            return builder.build();
        }

        @Override
        public int getNextId(PushMessage pushMessage) {
            return NotificationIdGenerator.nextID();
        }
    }
}
