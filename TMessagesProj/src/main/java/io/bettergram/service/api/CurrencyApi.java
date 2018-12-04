package io.bettergram.service.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import io.bettergram.data.CryptoCurrency;
import io.bettergram.data.CryptoCurrencyData;
import io.bettergram.data.CryptoCurrencyData__JsonHelper;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.telegram.messenger.NotificationCenter;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.bettergram.service.CryptoCurrencyDataService.CRYPTO_PREF;
import static io.bettergram.service.CryptoCurrencyDataService.KEY_CRYPTO_CURRENCIES;
import static io.bettergram.telegram.messenger.ApplicationLoader.okhttp_client;

public final class CurrencyApi {
    private static final String CURRENCY_SEARCH_URL = "https://alpha-api.livecoinwatch.com/currencies?search=";
    private static final String CURRENCY_COINS_URL = "https://alpha-api.livecoinwatch.com/coins";
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private static SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(CRYPTO_PREF, Context.MODE_PRIVATE);

    public static void search(String query) {
        if (query.length() == 0) {
            cancelCallsByTag("request_currency_search");
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateCurrencyDataToBackup);
            return;
        }
        THREAD_POOL.submit(() -> {
            try {
                String savedCryptoJson = preferences.getString(KEY_CRYPTO_CURRENCIES, null);
                CryptoCurrencyData data = CryptoCurrencyData__JsonHelper.parseFromJson(savedCryptoJson);
                if (data != null && data.data != null) {
                    List<String> codeList = new ArrayList<>();
                    for (int i = 0, size = data.data.size(); i < size; i++) {
                        final CryptoCurrency crypto = data.data.get(i);
                        if (crypto.name.toLowerCase().contains(query.toLowerCase()) || crypto.code.toLowerCase().contains(query.toLowerCase())) {
                            codeList.add(data.data.get(i).code);
                        }
                    }
                    fetchCoinsFromCodes(codeList, new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (!call.isCanceled()) e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String jsonCoins = null;
                            if (!call.isCanceled() && response != null && response.isSuccessful() && response.body() != null) {
                                jsonCoins = response.body().string();
                            }
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.currencySearchResultsUpdate, jsonCoins);
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void cancelCallsByTag(String tag) {
        //When you want to cancel:
        //A) go through the queued calls and cancel if the tag matches:
        for (Call call : okhttp_client().dispatcher().queuedCalls()) {
            if (Objects.equals(call.request().tag(), tag)) {
                call.cancel();
            }
        }

        //B) go through the running calls and cancel if the tag matches:
        for (Call call : okhttp_client().dispatcher().runningCalls()) {
            if (Objects.equals(call.request().tag(), tag)) {
                call.cancel();
            }
        }
    }

    private static void fetchCoinsFromCodes(List<String> codeList, Callback callback) {
        cancelCallsByTag("request_currency_search");
        String codes = TextUtils.join(",", codeList);
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(CURRENCY_COINS_URL)).newBuilder();
        urlBuilder.addQueryParameter("sort", "rank");
        urlBuilder.addQueryParameter("order", "ascending");
        urlBuilder.addQueryParameter("offset", "0");
        urlBuilder.addQueryParameter("limit", "10");
        urlBuilder.addQueryParameter("currency", "USD");
        urlBuilder.addQueryParameter("only", codes);
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder().tag("request_currency_search").url(url).build();
        okhttp_client().newCall(request).enqueue(callback);
    }
}
