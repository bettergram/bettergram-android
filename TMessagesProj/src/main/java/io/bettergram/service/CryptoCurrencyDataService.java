package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.bettergram.data.CryptoCurrency;
import io.bettergram.data.CryptoCurrencyData;
import io.bettergram.data.CryptoCurrencyData__JsonHelper;
import io.bettergram.data.CryptoCurrencyInfo;
import io.bettergram.data.CryptoCurrencyInfoData;
import io.bettergram.data.CryptoCurrencyInfoData__JsonHelper;
import io.bettergram.data.CryptoCurrencyInfoResponse;
import io.bettergram.data.CryptoCurrencyInfoResponse__JsonHelper;
import io.bettergram.service.api.CurrencyApi;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.telegram.messenger.NotificationCenter;
import io.bettergram.utils.CollectionUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.telegram.messenger.ApplicationLoader.okhttp_client;

public class CryptoCurrencyDataService extends BaseDataService {

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
    public static final String EXTRA_RUN_FROM_START = "EXTRA_RUN_FROM_START";

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.CryptoCurrencyDataService";

    public static final String CURRENCY_URL = "https://http-api.livecoinwatch.com/currencies?type=coin";
    public static final String CURRENCY_COINS_URL = "https://http-api.livecoinwatch.com/bettergram/coins";
    public static final String CURRENCY_STATS_URL = "https://http-api.livecoinwatch.com/stats";

    public static boolean isCurrentActive = false;
    private static final OkHttpClient client = okhttp_client();
    public static final int notify = 60000;
    private Timer mTimer = null;

    private static SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(CRYPTO_PREF, Context.MODE_PRIVATE);

    public CryptoCurrencyDataService() {
        super("CryptoCurrencyDataService");
    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

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
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateCurrencyData, savedCryptoInfoJson);
                }
                canStart = true;
            }
        }
    }

    private void startTimer(Intent intent) {
        mTimer.scheduleAtFixedRate(new TimeDisplay(intent), 0, notify);   //Schedule task
    }

    private List<CryptoCurrencyInfo> addIcons(List<CryptoCurrencyInfo> list, List<CryptoCurrency> currencies) {
        if (currencies == null) {
            return new ArrayList<>();
        }

        for (int i = 0, size = list.size(); i < size; i++) {
            final int index = i;
            CryptoCurrency foundCurrency = CollectionUtil.find(currencies, item -> list.get(index).code.equals(item.code));
            if (foundCurrency != null) {
                //list.get(index).icon = foundCurrency.icon;
                list.get(index).name = foundCurrency.name;
            }
        }
        return list;
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {

        Intent intent;

        TimeDisplay(Intent intent) {
            this.intent = intent;
        }

        @Override
        public void run() {
            final boolean runFromStart = preferences.getBoolean(EXTRA_RUN_FROM_START, false);
            if (runFromStart || isCurrentActive) {
                String savedCryptoInfoJson = preferences.getString(KEY_CRYPTO_CURRENCIES_SAVED, null);
                if (!isEmpty(savedCryptoInfoJson)) {
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateCurrencyData, savedCryptoInfoJson);
                }
                boolean fetchCryptoCurrencies = intent.getBooleanExtra(KEY_CRYPTO_CURRENCIES, false);
                String savedCryptoJson = preferences.getString(KEY_CRYPTO_CURRENCIES, null);
                List<CryptoCurrency> currencies = new ArrayList<>();
                CryptoCurrencyData currencyData = null;
                if (fetchCryptoCurrencies || isEmpty(savedCryptoJson)) {
                    Request request = new Request.Builder().url(CURRENCY_URL).build();
                    try {
                        Response response = okhttp_client().newCall(request).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            //final JsonParser parser = new JsonFactory().createParser(response.body().byteStream());
                            String fetchedCryptoJson = response.body().string();
                            currencyData = CryptoCurrencyData__JsonHelper.parseFromJson(fetchedCryptoJson);
                            if (currencyData != null) {
                                preferences.edit().putString(KEY_CRYPTO_CURRENCIES, fetchedCryptoJson).apply();
                                savedCryptoJson = fetchedCryptoJson;
                            } else {
                                preferences.edit().putString(KEY_CRYPTO_CURRENCIES, null).apply();
                                savedCryptoJson = null;
                            }
                        } else {
                            if (response.code() == 410) {
                                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateToLatestApiVersion);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (currencyData != null) {
                        //if (!isEmpty(savedCryptoJson)) {
                        //CryptoCurrencyData currencyData = CryptoCurrencyData__JsonHelper.parseFromJson(savedCryptoJson);
                        currencies.addAll(currencyData.data);
                    } else if (!isEmpty(savedCryptoJson)) {
                        currencyData = CryptoCurrencyData__JsonHelper.parseFromJson(savedCryptoJson);
                        currencies.addAll(currencyData.data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                String sortBy = intent.getStringExtra(EXTRA_SORT_BY);
                String orderBy = intent.getStringExtra(EXTRA_ORDER_BY);
                int offset = intent.getIntExtra(EXTRA_OFFSET, 0);
                int limit = intent.getIntExtra(EXTRA_LIMIT, 10);
                String favorites = intent.getStringExtra(EXTRA_FAVORITE);
                String currency = intent.getStringExtra(EXTRA__CURRENCY);

                HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(CURRENCY_COINS_URL)).newBuilder();
                urlBuilder.addQueryParameter("sort", !isEmpty(sortBy) ? sortBy : "rank");
                urlBuilder.addQueryParameter("order", !isEmpty(orderBy) ? orderBy : "ascending");
                urlBuilder.addQueryParameter("offset", String.valueOf(offset));
                urlBuilder.addQueryParameter("limit", String.valueOf(limit));
                urlBuilder.addQueryParameter("favorites", !isEmpty(favorites) ? favorites : String.valueOf(false));
                urlBuilder.addQueryParameter("currency", currency);

                CurrencyApi.cancelCallsByTag("request_currency_coins");
                String url = urlBuilder.build().toString();
                Request request = new Request.Builder().tag("request_currency_coins").url(url).build();

                okhttp_client().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!call.isCanceled() && response != null && response.isSuccessful() && response.body() != null) {
                                //JsonParser parser = new JsonFactory().createParser(response.body().byteStream());
                                String fetchedCurrencyJson = response.body().string();
                                CryptoCurrencyInfoResponse cryptoResponse = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(fetchedCurrencyJson);
                                if (cryptoResponse != null) {
                                    cryptoResponse.data.list = addIcons(cryptoResponse.data.list, currencies);
                                    String savedFaveCryptoJson = preferences.getString(KEY_CRYPTO_CURRENCIES_FAVORITE, null);
                                    if (!isEmpty(savedFaveCryptoJson)) {
                                        CryptoCurrencyInfoData data = CryptoCurrencyInfoData__JsonHelper.parseFromJson(savedFaveCryptoJson);
                                        if (data != null && data.list != null && !data.list.isEmpty()) {
                                            for (int i = 0, size = cryptoResponse.data.list.size(); i < size; i++) {
                                                final int index = i;
                                                CryptoCurrencyInfo inf = CollectionUtil.find(data.list,
                                                        item -> cryptoResponse.data.list
                                                                .get(index).code.equals(item.code));
                                                cryptoResponse.data.list.get(index).favorite = inf != null;
                                            }
                                        }
                                    }
                                    String json = CryptoCurrencyInfoResponse__JsonHelper.serializeToJson(cryptoResponse);
                                    preferences.edit().putString(KEY_CRYPTO_CURRENCIES_SAVED, json).apply();
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateCurrencyData, json);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void saveCurrencies(List<CryptoCurrencyInfo> list) {
        String json = preferences.getString(KEY_CRYPTO_CURRENCIES_SAVED, null);
        if (!isEmpty(json)) {
            try {
                CryptoCurrencyInfoResponse data = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(json);
                if (data.data == null) {
                    data.data = new CryptoCurrencyInfoData();
                }
                if (data.data.list == null) {
                    data.data.list = new ArrayList<>();
                }
                data.data.list.clear();
                data.data.list.addAll(list);
                json = CryptoCurrencyInfoResponse__JsonHelper.serializeToJson(data);
                preferences.edit().putString(KEY_CRYPTO_CURRENCIES_SAVED, json).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void faveCurrency(boolean fave, CryptoCurrencyInfo crypto) {
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
        if (data.list == null) {
            data.list = new ArrayList<>();
        }

        if (fave) {
            data.list.add(crypto);
        } else {
            for (Iterator<CryptoCurrencyInfo> iter = data.list.listIterator(); iter.hasNext(); ) {
                CryptoCurrencyInfo element = iter.next();
                if (element.code.equals(crypto.code)) {
                    iter.remove();
                }
            }
        }
        try {
            json = CryptoCurrencyInfoData__JsonHelper.serializeToJson(data);
            preferences.edit().putString(KEY_CRYPTO_CURRENCIES_FAVORITE, json).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
