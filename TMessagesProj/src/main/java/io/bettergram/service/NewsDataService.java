package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bettergram.data.News;
import io.bettergram.data.NewsData;
import io.bettergram.data.NewsData__JsonHelper;
import io.bettergram.data.NewsList;
import io.bettergram.data.NewsList__JsonHelper;
import io.bettergram.data.Source;
import io.bettergram.messenger.BuildConfig;
import io.bettergram.service.api.NewsApi;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.telegram.messenger.NotificationCenter;
import io.bettergram.utils.CollectionUtil;
import io.bettergram.utils.io.IOUtils;
import okhttp3.Request;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.telegram.messenger.ApplicationLoader.okhttp_client;
import static io.bettergram.utils.AeSimpleSHA1.SHA1;

public class NewsDataService extends BaseDataService {

    public static final String NEWS_PREF = "NEWS_PREF";
    public static final String KEY_FEED_XML_SET = "KEY_FEED_XML_SET";
    public static final String KEY_SAVED_LIST = "KEY_SAVED_LIST";

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.NewsDataService";

    public static boolean isIntentServiceRunning = false;

    private SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(NEWS_PREF, Context.MODE_PRIVATE);

    public NewsDataService() {
        super("NewsDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isIntentServiceRunning) {
            isIntentServiceRunning = true;

            String jsonRaw = preferences.getString(KEY_SAVED_LIST, null);
            if (!isEmpty(jsonRaw)) {
                publishResults(jsonRaw, NOTIFICATION, RESULT);
            }

            List<News> articles = new ArrayList<>();

            try {
                Request request = new Request.Builder().url(NewsApi.LIVE_COIN_WATCH_NEWS_URL).build();
                Response response = okhttp_client().newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    NewsData data = NewsData__JsonHelper.parseFromJson(json);

                    for (int i = 0, size_i = data.news.size(); i < size_i; i++) {

                        String url = data.news.get(i);
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                                .openConnection();
                        urlConnection.setRequestProperty("User-Agent", "Bettergram");
                        urlConnection.connect();
                        InputStream in = urlConnection.getInputStream();

                        String urlHash = url;
                        try {
                            urlHash = SHA1(urlHash);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        String xmlFetched = IOUtils.toString(in, "UTF-8");
                        Set<String> savedStringSet = preferences
                                .getStringSet(KEY_FEED_XML_SET + urlHash, null);
                        String xmlSaveHash =
                                savedStringSet != null ? savedStringSet.toArray(new String[0])[0]
                                        : null;
                        String xmlSaved =
                                savedStringSet != null ? savedStringSet.toArray(new String[0])[1]
                                        : null;
                        String xmlFinal = null;
                        try {
                            if (isEmpty(xmlSaved) || !SHA1(xmlFetched).equals(xmlSaveHash)) {
                                Set<String> stringSet = new HashSet<>();
                                stringSet.add(SHA1(xmlFetched));
                                stringSet.add(xmlFetched);
                                preferences.edit().putStringSet(KEY_FEED_XML_SET + urlHash, stringSet)
                                        .apply();
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

                        List<News> temp = new ArrayList<>();

                        Document document = Jsoup.parse(xmlFinal, "", Parser.xmlParser());
                        Elements channelElements = document.getElementsByTag("channel");
                        if (channelElements != null && channelElements.size() > 0) {
                            Elements itemElements = channelElements.get(0).getElementsByTag("item");
                            for (Element itemElement : itemElements) {
                                News newsItem = new News();
                                newsItem.title = itemElement
                                        .getElementsByTag("title")
                                        .get(0)
                                        .html();
                                newsItem.url = itemElement
                                        .getElementsByTag("link")
                                        .get(0)
                                        .html();
                                newsItem.source = new Source();
                                newsItem.source.name = channelElements
                                        .get(0)
                                        .getElementsByTag("title")
                                        .get(0)
                                        .html();
                                newsItem.publishedAt = itemElement
                                        .getElementsByTag("pubDate")
                                        .get(0)
                                        .html();
                                temp.add(newsItem);
                            }
                        }
                        articles.addAll(temp);
                    }

                    NewsList newsList = new NewsList();
                    newsList.articles = articles;

                    if (isEmpty(jsonRaw)) {
                        publishResults(NewsList__JsonHelper.serializeToJson(newsList), NOTIFICATION, RESULT);
                    } else {
                        NewsList savedNewsList = NewsList__JsonHelper.parseFromJson(jsonRaw);
                        for (int i = 0, size = newsList.articles.size(); i < size; i++) {
                            final News article = newsList.articles.get(i);
                            News foundNews = CollectionUtil.find(savedNewsList.articles, item -> article.url.equals(item.url));
                            if (foundNews != null) {
                                newsList.articles.get(i).urlToImage = foundNews.urlToImage;
                            }
                        }
                        newsList.sortArticlesByDate();
                        publishResults(NewsList__JsonHelper.serializeToJson(newsList), NOTIFICATION, RESULT);
                    }
                } else {
                    if (response.code() == 410) {
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateToLatestApiVersion);
                    }
                }
            } catch (IOException e) {
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

                                String thumb = null;

                                //for news from https://www.coindesk.com
                                Elements elementsFromAttrs = document.head().getElementsByAttributeValue("type", "application/ld+json");
                                if (elementsFromAttrs != null) {
                                    for (Element element : elementsFromAttrs) {
                                        String json = element.html();
                                        if (!isEmpty(json) && json
                                                .contains("\"@type\":\"NewsArticle\"")) {
                                            Pattern pattern = Pattern
                                                    .compile("\"thumbnailUrl\":\"(.*?)\"");
                                            Matcher matcher = pattern.matcher(json);
                                            if (matcher.find()) {
                                                thumb = json.substring(
                                                        matcher.start(),
                                                        matcher.end())
                                                        .replace("\"thumbnailUrl\":", "")
                                                        .replaceAll("^\"|\"$", "")
                                                        .replaceAll("\\\\/", "/");
                                                break;
                                            }
                                        }
                                    }
                                }

                                // for news from https://livecoinwatch.com and https://coincentral.com
                                if (isEmpty(thumb)) {
                                    Elements origImageUrl = document.head().getElementsByAttributeValue("property", "og:image");
                                    String file = origImageUrl.attr("content");
                                    if (isEmpty(file)) {
                                        file = document.head().getElementsByAttributeValue("meta", "twitter:image").attr("content");
                                    }

                                    if (!isEmpty(file)) {
                                        file = file.replaceAll("-\\d+[Xx]\\d+\\.", "");
                                        file = file.substring(file.lastIndexOf('/') + 1, file.length());
                                        file = file.replaceAll(".(png|gif|jpg|jpeg)", "");
                                    }

                                    elementsFromAttrs = document.body().getElementsByAttributeValue("class", "td-post-featured-image");
                                    if (isEmpty(elementsFromAttrs.html())) {
                                        elementsFromAttrs = document.body().getElementsByAttributeValue("class", "fl-photo-content fl-photo-img-png");
                                    }
                                    boolean found = false;
                                    for (int w = 0, size_w = elementsFromAttrs.size(); w < size_w;
                                         w++) {
                                        Element element = elementsFromAttrs.get(w);
                                        Elements imgElements = element.getElementsByTag("img");
                                        String sourceSet = imgElements.attr("srcset");
                                        List<String> sources = Arrays.asList(sourceSet.split("\\s*,\\s*"));
                                        int smallest = Integer.MAX_VALUE;
                                        int secondSmallest = Integer.MAX_VALUE;
                                        for (int x = 0, size_x = sources.size(); x < size_x; x++) {
                                            String[] source = sources.get(x).split(" ");
                                            if (source.length > 1) {
                                                int width = Integer.valueOf(source[1].replaceAll("\\D+", ""));
                                                if (width == smallest) {
                                                    secondSmallest = smallest;
                                                } else if (width < smallest) {
                                                    secondSmallest = smallest;
                                                    smallest = width;
                                                } else if (width < secondSmallest) {
                                                    secondSmallest = width;
                                                }
                                            }
                                        }
                                        if (secondSmallest > 0) {
                                            for (int x = 0, size_x = sources.size(); x < size_x;
                                                 x++) {
                                                if (sources.get(x).contains(secondSmallest + "w")) {
                                                    String[] source = sources.get(x).split(" ");
                                                    if (source.length > 1 && source[0].contains(file)) {
                                                        thumb = source[0];
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (found) {
                                            break;
                                        }
                                    }
                                }

                                // for https://www.ccn.com
                                if (isEmpty(thumb)) {
                                    elementsFromAttrs = document.getElementsByAttributeValue("class", "post-thumbnail");
                                    if (!isEmpty(elementsFromAttrs.html()) && elementsFromAttrs.size() > 0) {
                                        Elements elements = elementsFromAttrs.get(0).getElementsByTag("img");
                                        String temp = elements.attr("src");
                                        if (!isEmpty(temp)) {
                                            thumb = temp;
                                        }
                                    }
                                }

                                if (isEmpty(thumb)) {
                                    Elements metas = document.head().getElementsByTag("meta");
                                    for (Element meta : metas) {
                                        Elements attribute = meta.getElementsByAttributeValue("property", "og:image");
                                        String content = attribute.attr("content");
                                        if (!isEmpty(content)) {
                                            thumb = content;
                                            break;
                                        }
                                    }
                                }

                                if (BuildConfig.DEBUG) {
                                    Log.i("news", "thumb url: " + thumb);
                                }
                                articles.get(i).urlToImage = thumb;
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                NewsList newsList = new NewsList();
                newsList.articles = articles;
                newsList.sortArticlesByDate();

                try {
                    String json = NewsList__JsonHelper.serializeToJson(newsList);
                    preferences.edit().putString(KEY_SAVED_LIST, json).apply();
                    publishResults(json, NOTIFICATION, RESULT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    int counter = 0;
                    if (!isEmpty(jsonRaw)) {
                        NewsList rawNewsList = NewsList__JsonHelper.parseFromJson(jsonRaw);
                        for (int i = 0, size = newsList.articles.size(); i < size; i++) {
                            final News indexedNews = newsList.articles.get(i);
                            final News foundNews = CollectionUtil.find(rawNewsList.articles, item -> indexedNews.title.equals(item.title));
                            if (foundNews == null) {
                                counter++;
                            }
                        }
                    } else {
                        if (newsList.articles != null && !newsList.articles.isEmpty()) {
                            counter = newsList.articles.size() - 1;
                        }
                    }
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateBottombarCounter, counter, "news");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isIntentServiceRunning = false;
        }
    }
}
