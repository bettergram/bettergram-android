/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package io.bettergram.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.iid.FirebaseInstanceId;
import io.bettergram.service.CryptoDataService;
import io.bettergram.service.NewsDataService;
import io.bettergram.service.ResourcesDataService;
import io.bettergram.service.YoutubeDataService;
import io.bettergram.telegram.tgnet.ConnectionsManager;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.Components.ForegroundDetector;
import io.fabric.sdk.android.Fabric;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.bettergram.service.CryptoDataService.EXTRA_LIMIT;

public class ApplicationLoader extends Application {

    public static OkHttpClient okhttp_singleton;

    @SuppressLint("StaticFieldLeak")
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private static volatile boolean applicationInited = false;

    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;
    public static volatile boolean externalInterfacePaused = true;
    public static volatile boolean mainInterfacePausedStageQueue = true;
    public static volatile long mainInterfacePausedStageQueueTime;

    public static String packageName() {
        return ApplicationLoader.applicationContext.getPackageName();
    }

    public static String version() {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(packageName(), 0);
            return String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return new File("/data/data/io.bettergram.messenger/files");
    }

    public static void postInitApplication() {
        if (applicationInited) {
            return;
        }

        applicationInited = true;

        try {
            LocaleController.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            final BroadcastReceiver mReceiver = new ScreenReceiver();
            applicationContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PowerManager pm = (PowerManager) ApplicationLoader.applicationContext
                    .getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("screen state = " + isScreenOn);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }

        SharedConfig.loadConfig();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            UserConfig.getInstance(a).loadConfig();
            MessagesController.getInstance(a);
            ConnectionsManager.getInstance(a);
            TLRPC.User user = UserConfig.getInstance(a).getCurrentUser();
            if (user != null) {
                MessagesController.getInstance(a).putUser(user, true);
                MessagesController.getInstance(a).getBlockedUsers(true);
                SendMessagesHelper.getInstance(a).checkUnsentMessages();
            }
        }

        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
        app.initPlayServices();
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("app initied");
        }

        MediaController.getInstance();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            ContactsController.getInstance(a).checkAppAccount();
            DownloadController.getInstance(a);
        }

        WearDataLayerListenerService.updateWatchConnectionState();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)  // Enables Crashlytics debugger
                .build();
        Fabric.with(fabric);

        applicationContext = getApplicationContext();

        updateAndroidSecurityProvider();

        NativeLoader.initNativeLibs(ApplicationLoader.applicationContext);
        ConnectionsManager.native_setJava(false);
        new ForegroundDetector(this);

        applicationHandler = new Handler(applicationContext.getMainLooper());

        AndroidUtilities.runOnUIThread(ApplicationLoader::startPushService);
    }

    public static void startPushService() {
        SharedPreferences preferences = MessagesController.getGlobalNotificationsSettings();
        if (preferences.getBoolean("pushService", true)) {
            try {
                applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
            } catch (Throwable ignore) {

            }
        } else {
            stopPushService();
        }
    }

    public static void stopPushService() {
        applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));

        PendingIntent pintent = PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0);
        AlarmManager alarm = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
            AndroidUtilities.checkDisplaySize(applicationContext, newConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayServices() {
        AndroidUtilities.runOnUIThread(() -> {
            if (checkPlayServices()) {
                final String currentPushString = SharedConfig.pushString;
                if (!TextUtils.isEmpty(currentPushString)) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("GCM regId = " + currentPushString);
                    }
                } else {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("GCM Registration not found.");
                    }
                }
                Utilities.globalQueue.postRunnable(() -> {
                    try {
                        String token = FirebaseInstanceId.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            GcmInstanceIDListenerService.sendRegistrationToServer(token);
                        }
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                });
            } else {
                if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("No valid Google Play Services APK found.");
                }
            }
        }, 1000);
    }

    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            return resultCode == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }

    private void updateAndroidSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(applicationContext);
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException
                | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    protected static List<ConnectionSpec> connectionSpecs() {
        // Necessary because our servers don't have the right cipher suites.
        // https://github.com/square/okhttp/issues/4053
        List<CipherSuite> cipherSuites = new ArrayList<>(ConnectionSpec.MODERN_TLS.cipherSuites());
        cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA);
        cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA);
        cipherSuites.add(CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA);

        ConnectionSpec legacyTls = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(cipherSuites.toArray(new CipherSuite[0]))
                .build();

        return Arrays.asList(legacyTls, ConnectionSpec.CLEARTEXT);
    }

    public static void warmupBettergramData(Activity activity) {
        Intent intent = new Intent(activity, CryptoDataService.class);
        intent.putExtra(EXTRA_LIMIT, 100);
        activity.startService(intent);

        Intent intent2 = new Intent(activity, NewsDataService.class);
        activity.startService(intent2);

        Intent intent3 = new Intent(activity, YoutubeDataService.class);
        activity.startService(intent3);

        Intent intent4 = new Intent(activity, ResourcesDataService.class);
        activity.startService(intent4);
    }

    public static OkHttpClient okhttp_client() {
        if (okhttp_singleton == null) {
            synchronized (OkHttpClient.class) {
                if (okhttp_singleton == null) {
                    okhttp_singleton = new OkHttpClient
                            .Builder()
                            .connectTimeout(5, TimeUnit.MINUTES)
                            .writeTimeout(5, TimeUnit.MINUTES)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .connectionSpecs(connectionSpecs())
                            .build();
                }
            }
        }
        return okhttp_singleton;
    }
}
