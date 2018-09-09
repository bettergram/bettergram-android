package ru.johnlife.lifetools.tools;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

public class Downloader {
    public static void download(String url, String title, String description, String fileName) {
        Context context = DefaultContext.get();
        File dir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDescription(description)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        downloadManager.enqueue(request);
    }
}
