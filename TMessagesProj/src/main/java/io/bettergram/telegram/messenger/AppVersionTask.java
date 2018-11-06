package io.bettergram.telegram.messenger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AppVersionTask extends AsyncTask<Void, String, String> {

    private OnVersionListener listener;

    public AppVersionTask(OnVersionListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String newVersion = null;
        try {
            newVersion = getPlayStoreAppVersion("https://play.google.com/store/apps/details?id=" + ApplicationLoader.packageName() + "&hl=en");
            return newVersion;
        } catch (Exception e) {
            e.printStackTrace();
            return newVersion;
        }
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);
        if (onlineVersion != null && !onlineVersion.isEmpty()) {
            if (Long.valueOf(extractDigits(ApplicationLoader.version())) < Long.valueOf(extractDigits(onlineVersion))) {
                if (listener != null) {
                    listener.onNewUpdate(true);
                }
                return;
            }
        }
        if (listener != null) {
            listener.onNewUpdate(false);
        }
    }

    private String extractDigits(String s) {
        return s.replaceAll("\\D+", "");
    }

    private String getAppVersion(String patternString, String inputString) {
        try {
            //Create a pattern
            Pattern pattern = Pattern.compile(patternString);
            if (null == pattern) {
                return null;
            }

            //Match the pattern string in provided string
            Matcher matcher = pattern.matcher(inputString);
            if (null != matcher && matcher.find()) {
                return matcher.group(1);
            }

        } catch (PatternSyntaxException ex) {

            ex.printStackTrace();
        }

        return null;
    }


    private String getPlayStoreAppVersion(String appUrlString) throws IOException {
        final String currentVersion_PatternSeq = "<div[^>]*?>Current\\sVersion</div><span[^>]*?>(.*?)><div[^>]*?>(.*?)><span[^>]*?>(.*?)</span>";
        final String appVersion_PatternSeq = "htlgb\">([^<]*)</s";
        String playStoreAppVersion = null;

        BufferedReader inReader = null;
        URLConnection uc = null;
        StringBuilder urlData = new StringBuilder();

        final URL url = new URL(appUrlString);
        uc = url.openConnection();
        if (uc == null) {
            return null;
        }
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
        inReader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        if (null != inReader) {
            String str = "";
            while ((str = inReader.readLine()) != null) {
                urlData.append(str);
            }
        }

        // Get the current version pattern sequence
        String versionString = getAppVersion(currentVersion_PatternSeq, urlData.toString());
        if (null == versionString) {
            return null;
        } else {
            // get version from "htlgb">X.X.X</span>
            playStoreAppVersion = getAppVersion(appVersion_PatternSeq, versionString);
        }

        return playStoreAppVersion;
    }

    public static void openAppInPlayStore(Activity activity) {
        final String appPackageName = ApplicationLoader.packageName(); // getPackageName() from Context or Activity object
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public interface OnVersionListener {
        void onNewUpdate(boolean shouldUpdate);
    }
}