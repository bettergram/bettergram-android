package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by yanyu on 5/12/2016.
 */
public class OpenInBrowser {

    private static final String MAILTO_PREFIX = "mailto:";

    public static void url(Context context, String url) {
        if (url.toLowerCase().startsWith(MAILTO_PREFIX)) {
            mail(context, url);
            return;
        }
        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private static void mail(Context context, String url) {
        if (!url.startsWith(MAILTO_PREFIX)) {
            return;
        }
        context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
    }
}
