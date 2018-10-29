package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import io.bettergram.data.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.bettergram.telegram.messenger.ApplicationLoader;

import java.io.IOException;
import java.util.*;

import static android.text.TextUtils.isEmpty;

public class CryptoDataService extends BaseDataService {

    public static final String CRYPTO_PREF = "CRYPTO_PREF";
    public static final String KEY_CRYPTO_CURRENCIES = "KEY_CRYPTO_CURRENCIES";
    public static final String KEY_CRYPTO_CURRENCIES_FAVORITE = "KEY_CRYPTO_CURRENCIES_FAVORITE";
    public static final String KEY_CRYPTO_CURRENCIES_SAVED = "KEY_CRYPTO_CURRENCIES_SAVED";

    public static final String EXTRA_FETCH_CRYPTO_CURRENCIES = "EXTRA_FETCH_CRYPTO_CURRENCIES";
    public static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    public static final String EXTRA_ORDER_BY = "EXTRA_ORDER_BY";
    public static final String EXTRA_OFFSET = "EXTRA_OFFSET";
    public static final String EXTRA_LIMIT = "EXTRA_LIMIT";
    public static final String EXTRA_FAVORITE = "EXTRA_FAVORITE";
    public static final String EXTRA__CURRENCY = "EXTRA__CURRENCY";

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.CryptoDataService";

    public static final String CRYPTO_CURRENCIES_URL = "https://http-api.livecoinwatch.com/currencies";
    public static final String CRYPTO_COINS_URL = "https://http-api.livecoinwatch.com/bettergram/coins";

    public static final int notify = 60000;
    private Timer mTimer = null;

    private static SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(CRYPTO_PREF, Context.MODE_PRIVATE);

    public CryptoDataService() {
        super("CryptoDataService");
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mTimer = new Timer();   //recreate new
        boolean canStart = true;
        while (canStart) {
            try {
                startTimer(intent);
                canStart = false;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                String savedCryptoInfoJson = preferences.getString(KEY_CRYPTO_CURRENCIES_SAVED, null);
                if (!isEmpty(savedCryptoInfoJson)) {
                    publishResults(savedCryptoInfoJson, NOTIFICATION, RESULT);
                }
                canStart = true;
            }
        }
    }

    private void startTimer(Intent intent) {
        mTimer.scheduleAtFixedRate(new TimeDisplay(intent), 0, notify);   //Schedule task
    }

    private List<CryptoCurrencyInfo> addIcons(List<CryptoCurrencyInfo> list, List<CryptoCurrency> currencies) {
        if (currencies == null) return new ArrayList<>();

        for (int i = 0, size = list.size(); i < size; i++) {
            final int index = i;

            CryptoCurrency foundCurrency = CollectionUtil.find(currencies, (CryptoCurrency item) -> list.get(index).code.equals(item.code));

            if (foundCurrency != null) {
                list.get(index).icon = foundCurrency.icon;
                list.get(index).name = foundCurrency.name;
            }
        }
        return list;
    }

    public interface Predicate<T> {
        boolean contains(T item);
    }

    public static class CollectionUtil {

        public static <T> T find(final Collection<T> collection, final Predicate<T> predicate) {
            for (T item : collection) {
                if (predicate.contains(item)) {
                    return item;
                }
            }
            return null;
        }
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {

        Intent intent;

        TimeDisplay(Intent intent) {
            this.intent = intent;
        }

        @Override
        public void run() {
            String savedCryptoInfoJson = preferences.getString(KEY_CRYPTO_CURRENCIES_SAVED, null);
            if (!isEmpty(savedCryptoInfoJson)) {
                publishResults(savedCryptoInfoJson, NOTIFICATION, RESULT);
            }

            boolean fetchCryptoCurrencies = intent.getBooleanExtra(KEY_CRYPTO_CURRENCIES, false);
            String savedCryptoJson = preferences.getString(KEY_CRYPTO_CURRENCIES, null);
            List<CryptoCurrency> currencies = new ArrayList<>();

            OkHttpClient client = new OkHttpClient();

            if (fetchCryptoCurrencies || isEmpty(savedCryptoJson)) {
                Request request = new Request.Builder().url(CRYPTO_CURRENCIES_URL).build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        String fetchedCryptoJson = response.body().string();
                        preferences.edit().putString(KEY_CRYPTO_CURRENCIES, fetchedCryptoJson).apply();
                        savedCryptoJson = fetchedCryptoJson;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                if (!isEmpty(savedCryptoJson)) {
                    CryptoCurrencyData currencyData = CryptoCurrencyData__JsonHelper.parseFromJson(savedCryptoJson);
                    currencies.addAll(currencyData.data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String sortBy = intent.getStringExtra(EXTRA_SORT_BY);
            String orderBy = intent.getStringExtra(EXTRA_ORDER_BY);
            int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
            int limit = intent.getIntExtra(EXTRA_LIMIT, 10);
            String favorites = intent.getStringExtra(EXTRA_FAVORITE);
            String currency = intent.getStringExtra(EXTRA__CURRENCY);

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(CRYPTO_COINS_URL)).newBuilder();
            urlBuilder.addQueryParameter("sort", !isEmpty(sortBy) ? sortBy : "rank");
            urlBuilder.addQueryParameter("order", !isEmpty(orderBy) ? orderBy : "ascending");
            urlBuilder.addQueryParameter("offset", String.valueOf(offset));
            urlBuilder.addQueryParameter("limit", String.valueOf(limit));
            urlBuilder.addQueryParameter("favorites", !isEmpty(favorites) ? favorites : String.valueOf(false));
            urlBuilder.addQueryParameter("currency", currency);

            String url = urlBuilder.build().toString();
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String fetchedCurrencyJson = response.body().string();
                    CryptoCurrencyInfoResponse cryptoResponse = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(fetchedCurrencyJson);
                    cryptoResponse.data.favorites = addIcons(cryptoResponse.data.favorites, currencies);
                    cryptoResponse.data.list = addIcons(cryptoResponse.data.list, currencies);
                    if (!cryptoResponse.data.favorites.isEmpty()) {
                        for (int i = 0, size = cryptoResponse.data.favorites.size(); i < size; i++) {
                            cryptoResponse.data.favorites.get(i).favorite = true;
                        }
                    }
                    String savedFaveCryptoJson = preferences.getString(KEY_CRYPTO_CURRENCIES_FAVORITE, null);
                    if (!isEmpty(savedFaveCryptoJson)) {
                        CryptoCurrencyInfoData data = CryptoCurrencyInfoData__JsonHelper.parseFromJson(savedFaveCryptoJson);
                        if (data != null && !data.favorites.isEmpty()) {
                            for (int i = 0, size = cryptoResponse.data.list.size(); i < size; i++) {
                                final int index = i;
                                CryptoCurrencyInfo inf = CollectionUtil.find(data.favorites, (CryptoCurrencyInfo item) -> cryptoResponse.data.list.get(index).code.equals(item.code));
                                cryptoResponse.data.list.get(index).favorite = inf != null;
                            }
                        }
                    }

                    String json = CryptoCurrencyInfoResponse__JsonHelper.serializeToJson(cryptoResponse);
                    preferences.edit().putString(KEY_CRYPTO_CURRENCIES_SAVED, json).apply();

                    publishResults(json, NOTIFICATION, RESULT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean faveCrypto(boolean fave, CryptoCurrencyInfo crypto) {
        String json = preferences.getString(KEY_CRYPTO_CURRENCIES_FAVORITE, null);

        CryptoCurrencyInfoData data = null;
        if (!isEmpty(json)) {
            try {
                data = CryptoCurrencyInfoData__JsonHelper.parseFromJson(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (data == null) {
            data = new CryptoCurrencyInfoData();
        }
        if (data.favorites == null) {
            data.favorites = new ArrayList<>();
        }
        if (fave) {
            data.favorites.add(crypto);
        } else {
            data.favorites.remove(crypto);
        }
        try {
            preferences.edit().putString(KEY_CRYPTO_CURRENCIES_FAVORITE, CryptoCurrencyInfoData__JsonHelper.serializeToJson(data)).apply();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
