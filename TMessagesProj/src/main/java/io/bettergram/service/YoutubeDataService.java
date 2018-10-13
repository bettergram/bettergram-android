package io.bettergram.service;

import android.content.Intent;
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

public class YoutubeDataService extends BaseDataService {

    private static final String TAG = YoutubeDataService.class.getName();

    public static final String RESULT = "result";

    public static final String NOTIFICATION = "io.bettergram.service.YoutubeDataService";

    private static List<String> videoIds = new ArrayList<>();

    public YoutubeDataService() {
        super("YoutubeDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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

            publishResults(jsonResult, NOTIFICATION, RESULT);
        } catch (IOException | JSONException | FeedException | ParseException e) {
            e.printStackTrace();
        }
    }
}
