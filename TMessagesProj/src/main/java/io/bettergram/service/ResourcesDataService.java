package io.bettergram.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;

import io.bettergram.service.api.ResourcesApi;
import io.bettergram.telegram.messenger.ApplicationLoader;

import static android.text.TextUtils.isEmpty;

public class ResourcesDataService extends BaseDataService {

    public static final String RESOURCES_PREFERENCES = "RESOURCES_PREFERENCES";
    public static final String KEY_RESOURCES_JSON = "KEY_RESOURCES_JSON";

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.ResourcesDataService";

    private SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(RESOURCES_PREFERENCES, Context.MODE_PRIVATE);

    public ResourcesDataService() {
        super("ResourcesDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            String json = preferences.getString(KEY_RESOURCES_JSON, null);
            if (!isEmpty(json)) {
                publishResults(json, NOTIFICATION, RESULT);
            }

            json = ResourcesApi.getResourcesQuietly();
            if (!isEmpty(json)) {
                preferences.edit().putString(KEY_RESOURCES_JSON, json).apply();
            }
            publishResults(json, NOTIFICATION, RESULT);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
