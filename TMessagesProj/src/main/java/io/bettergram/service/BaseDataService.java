package io.bettergram.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

@SuppressLint("Registered")
public class BaseDataService extends IntentService {

    public BaseDataService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    protected void publishResults(String jsonResult, String action, String extraKey) {
        Intent intent = new Intent(action);
        intent.putExtra(extraKey, jsonResult);
        sendBroadcast(intent);
    }
}
