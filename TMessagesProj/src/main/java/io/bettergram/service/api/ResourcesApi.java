package io.bettergram.service.api;

import io.bettergram.utils.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class ResourcesApi {

    public static final String RESOURCES_BASE_URL = "https://api.bettergram.io/v1/resources";

    public static String getResourcesQuietly() throws IOException, JSONException {
        URL newsURL = new URL(RESOURCES_BASE_URL);
        JSONObject jsonData = new JSONObject(
                IOUtils.toString(
                        newsURL,
                        Charset.forName("UTF-8")
                )
        );

        return jsonData.toString();
    }
}
