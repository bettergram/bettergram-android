package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.bettergram.data.VideoData;
import io.bettergram.data.VideoData__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.api.VideosApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class YoutubeDataService extends BaseDataService {

    private static final String TAG = YoutubeDataService.class.getName();

    public static final String YOUTUBE_PREF = "YOUTUBE_PREF";

    public static final String KEY_VIDEO_JSON = "KEY_VIDEO_JSON";

    public static final String RESULT = "result";

    public static final String NOTIFICATION = "io.bettergram.service.YoutubeDataService";

    private static List<String> videoIds = new ArrayList<>();

    private SharedPreferences pref;

    public YoutubeDataService() {
        super("YoutubeDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        pref = getSharedPreferences(YOUTUBE_PREF, Context.MODE_PRIVATE);

        String jsonRaw = pref.getString(KEY_VIDEO_JSON, null);
        if (!isEmpty(jsonRaw)) {
            publishResults(jsonRaw, NOTIFICATION, RESULT);
        }

        String apiKey = getString(R.string.youtube_api_key);
        try {
            VideoData videoData = VideoData__JsonHelper.parseFromJson(VideosApi.getYoutubeRSSFeed());

            for (String video : videoData.videos) {
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(video)));
                for (SyndEntry entry : feed.getEntries()) {
                    videoIds.add(entry.getUri().replace("yt:video:", ""));
                }
            }

            JSONArray jsonArray = new JSONArray();

            for (String videoId : videoIds) {
                JSONObject jsonObject = new JSONObject(VideosApi.getDataQuietly(videoId, apiKey));
                jsonArray.put(jsonObject);
            }

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("videos", jsonArray);
            String jsonResult = jsonObject.toString();
            Log.i(TAG, "json: " + jsonResult);

            pref.edit().putString(KEY_VIDEO_JSON, jsonResult).apply();

            publishResults(jsonResult, NOTIFICATION, RESULT);
        } catch (IOException | JSONException | FeedException | ParseException e) {
            e.printStackTrace();
        }
    }
}
