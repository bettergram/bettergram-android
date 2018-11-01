package io.bettergram.service;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.telegram.messenger.ApplicationLoader.okhttp_client;
import static io.bettergram.utils.AeSimpleSHA1.SHA1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.bettergram.data.News;
import io.bettergram.data.NewsData;
import io.bettergram.data.NewsData__JsonHelper;
import io.bettergram.data.NewsList;
import io.bettergram.data.NewsList__JsonHelper;
import io.bettergram.data.Source;
import io.bettergram.service.api.NewsApi;
import io.bettergram.utils.io.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsDataService extends BaseDataService {

    public static final String NEWS_PREF = "NEWS_PREF";
    public static final String KEY_FEED_XML_SET = "KEY_FEED_XML_SET";
    public static final String KEY_SAVED_LIST = "KEY_SAVED_LIST";

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.NewsDataService";

    private SharedPreferences pref;

    public NewsDataService() {
        super("NewsDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        pref = getSharedPreferences(NEWS_PREF, Context.MODE_PRIVATE);

        String jsonRaw = pref.getString(KEY_SAVED_LIST, null);
        if (!isEmpty(jsonRaw)) {
            publishResults(jsonRaw, NOTIFICATION, RESULT);
        }

        List<News> articles = new ArrayList<>();

        try {
            String json = NewsApi.getNewsQuietly();

            NewsData data = NewsData__JsonHelper.parseFromJson(json);

            for (int i = 0, size_i = data.news.size(); i < size_i; i++) {

                String url = data.news.get(i);
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestProperty("User-Agent", "ROME");
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();

                String urlHash = url;
                try {
                    urlHash = SHA1(urlHash);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                String xmlFetched = IOUtils.toString(in, "UTF-8");
                Set<String> savedStringSet = pref.getStringSet(KEY_FEED_XML_SET + urlHash, null);
                String xmlSaveHash =
                        savedStringSet != null ? savedStringSet.toArray(new String[0])[0] : null;
                String xmlSaved =
                        savedStringSet != null ? savedStringSet.toArray(new String[0])[1] : null;
                String xmlFinal = null;
                try {
                    if (isEmpty(xmlSaved) || !SHA1(xmlFetched).equals(xmlSaveHash)) {
                        Set<String> stringSet = new HashSet<>();
                        stringSet.add(SHA1(xmlFetched));
                        stringSet.add(xmlFetched);
                        pref.edit().putStringSet(KEY_FEED_XML_SET + urlHash, stringSet).apply();
                        xmlFinal = xmlFetched;
                    } else {
                        xmlFinal = xmlSaved;
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                if (isEmpty(xmlFinal)) {
                    break;
                }

                InputStream stream = new ByteArrayInputStream(
                        xmlFinal.getBytes(StandardCharsets.UTF_8));

                SyndFeed feed = new SyndFeedInput().build(new XmlReader(stream, "text/xml"));

                List<News> temp = new ArrayList<>();

                for (int j = 0, size_j = feed.getEntries().size(); j < size_j; j++) {
                    SyndEntry entry = feed.getEntries().get(j);
                    News newsItem = new News();
                    newsItem.title = entry.getTitle();

                    SyndFeed source = entry.getSource();

                    Source newsSource = new Source();
                    newsSource.name = source != null ? source.getAuthor() : entry.getAuthor();
                    newsItem.source = newsSource;

                    newsItem.url = entry.getLink();
                    newsItem.urlToImage = source != null ? source.getImage().getUrl() : null;

                    newsItem.publishedAt = entry.getPublishedDate().toString();

                    temp.add(newsItem);
                }

                articles.addAll(temp);
            }

            NewsList newsList = new NewsList();
            newsList.articles = articles;
            publishResults(NewsList__JsonHelper.serializeToJson(newsList), NOTIFICATION, RESULT);
        } catch (IOException | JSONException | FeedException e) {
            e.printStackTrace();
        }

        if (!articles.isEmpty()) {

            for (int i = 0, size = articles.size(); i < size; i++) {
                try {
                    if (isEmpty(articles.get(i).urlToImage)) {

                        Request request = new Request.Builder()
                                .url(articles.get(i).url)
                                .build();

                        Response response = okhttp_client().newCall(request).execute();

                        if (response.body() != null && response.isSuccessful()) {
                            String result = response.body().string();
                            Document document = Jsoup.parse(result);
                            Elements metas = document.head().getElementsByTag("meta");
                            for (Element meta : metas) {
                                Elements attribute = meta
                                        .getElementsByAttributeValue("property", "og:image");
                                String content = attribute.attr("content");
                                if (!isEmpty(content)) {
                                    articles.get(i).urlToImage = content;
                                    break;
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            NewsList newsList = new NewsList();
            newsList.articles = articles;

            try {
                String json = NewsList__JsonHelper.serializeToJson(newsList);
                pref.edit().putString(KEY_SAVED_LIST, json).apply();
                publishResults(json, NOTIFICATION, RESULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
