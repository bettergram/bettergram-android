package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.crashlytics.android.Crashlytics;
import io.bettergram.data.*;
import io.bettergram.service.api.VideosApi;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.utils.Counter;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.telegram.messenger.ApplicationLoader.okhttp_client;

public class YoutubeDataService extends BaseDataService {

    private static final String TAG = YoutubeDataService.class.getName();

    public static final String YOUTUBE_PREF = "YOUTUBE_PREF";

    public static final String KEY_VIDEO_JSON = "KEY_VIDEO_JSON";

    public static final String RESULT = "result";

    public static final String NOTIFICATION = "io.bettergram.service.YoutubeDataService";

    private SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(YOUTUBE_PREF, Context.MODE_PRIVATE);

    public YoutubeDataService() {
        super("YoutubeDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String jsonRaw = preferences.getString(KEY_VIDEO_JSON, null);
        if (!isEmpty(jsonRaw)) {
            publishResults(jsonRaw, NOTIFICATION, RESULT);
        }

        try {
            VideoData videoData = VideoData__JsonHelper.parseFromJson(VideosApi.getYoutubeRSSFeed());

            VideoList videoList = new VideoList();
            videoList.videos = new ArrayList<>();

            for (String videoUrl : videoData.videos) {
                Request request = new Request.Builder()
                        .url(videoUrl)
                        .build();

                Response response = okhttp_client().newCall(request).execute();

                if (response.body() != null && response.isSuccessful()) {
                    String result = response.body().string();
                    Document document = Jsoup.parse(result, "", Parser.xmlParser());
                    for (Element element : document.getElementsByTag("entry")) {
                        if (!videoList.contains(element.getElementsByTag("yt:videoId").get(0).html())) {
                            Video video = new Video();
                            video.id = element
                                    .getElementsByTag("yt:videoId")
                                    .get(0)
                                    .html();
                            video.title = element
                                    .getElementsByTag("title")
                                    .get(0)
                                    .html();
                            video.channelTitle = element
                                    .getElementsByTag("author")
                                    .get(0)
                                    .getElementsByTag("name")
                                    .get(0)
                                    .html();
                            video.viewCount = Counter.format(element
                                    .getElementsByTag("media:group")
                                    .get(0)
                                    .getElementsByTag("media:community")
                                    .get(0)
                                    .getElementsByTag("media:statistics")
                                    .get(0)
                                    .attr("views"));
                            video.publishedAt = element
                                    .getElementsByTag("published")
                                    .get(0)
                                    .html();
                            videoList.videos.add(video);
                        }
                    }
                }
            }

            videoList.sortVideosByDate();

            String jsonResult = VideoList__JsonHelper.serializeToJson(videoList);
            preferences.edit().putString(KEY_VIDEO_JSON, jsonResult).apply();

            publishResults(jsonResult, NOTIFICATION, RESULT);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}
