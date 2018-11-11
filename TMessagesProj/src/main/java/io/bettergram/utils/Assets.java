package io.bettergram.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Assets {

    private AssetManager assetManager;
    private Properties properties;

    public Assets(Context context) {
        assetManager = context.getAssets();
    }

    public Assets fromFile(String file) throws IOException {
        InputStream inputStream = assetManager.open(file);
        properties = new Properties();
        properties.load(inputStream);
        return this;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
