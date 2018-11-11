package io.bettergram;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.UAirship;
import io.bettergram.messenger.BuildConfig;
import io.bettergram.messenger.R;
import io.bettergram.telegram.ui.ActionBar.Theme;
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
    }

    @Override
    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
        try {
            Assets assets = new Assets(context).fromFile("airshipconfig.properties");

            AirshipConfigOptions options = new AirshipConfigOptions.Builder()
                    .setDevelopmentAppKey(assets.getProperty("developmentAppKey"))
                    .setDevelopmentAppSecret(assets.getProperty("developmentAppSecret"))
                    .setProductionAppKey(assets.getProperty("productionAppKey"))
                    .setProductionAppSecret(assets.getProperty("productionAppSecret"))
                    .setInProduction(!BuildConfig.DEBUG)
                    .setGcmSender(assets.getProperty("fcmSenderId")) // FCM/GCM sender ID
                    .setNotificationIcon(R.drawable.notification)
                    //.setNotificationAccentColor(Theme.getColor(Theme.key_actionBarDefault))
                    .setNotificationChannel("customChannel")
                    .build();
            return options;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
