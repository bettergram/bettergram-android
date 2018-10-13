package io.bettergram.service.api;

import android.annotation.SuppressLint;
import io.bettergram.utils.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewsApi {

    //@formatter:off
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat FROM_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'");
    //@formatter:on
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat TO_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    private static final String LIVE_COIN_WATCH_NEWS_URL = "https://api.bettergram.io/v1/news";

    /**
     * Gets news related to cryptocurrency
     */
    public static String getNewsQuietly() throws IOException, JSONException {
        URL newsURL = new URL(LIVE_COIN_WATCH_NEWS_URL);
        JSONObject jsonData = new JSONObject(
                IOUtils.toString(
                        newsURL,
                        Charset.forName("UTF-8")
                )
        );

        return jsonData.toString();
    }

    /**
     * Gets formatted date
     */
    public static String getFormattedDate(String unformattedDate) throws ParseException {
        Date date = FROM_FORMAT.parse(unformattedDate);
        return TO_FORMAT.format(date);
    }
}
