package io.bettergram.service;

import android.content.Intent;
import android.support.annotation.Nullable;
import io.bettergram.service.api.ResourcesApi;
import org.json.JSONException;

import java.io.IOException;

public class ResourcesDataService extends BaseDataService {

    public static final String RESULT = "result";
    public static final String NOTIFICATION = "io.bettergram.service.ResourcesDataService";

    public ResourcesDataService() {
        super("ResourcesDataService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            String json = ResourcesApi.getResourcesQuietly();
            publishResults(json, NOTIFICATION, RESULT);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
