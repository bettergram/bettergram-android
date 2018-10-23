package io.bettergram.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Base64;
import io.bettergram.messenger.BuildConfig;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MailChimpService extends BaseDataService {

    public static final String EXTRA_SUBSCRIBE_NEWSLETTER = "EXTRA_SUBSCRIBE_NEWSLETTER";
    public static final String EXTRA_SUBSCRIBE_EMAIL = "EXTRA_SUBSCRIBE_EMAIL";

    public static final String RESULT = "result";
    public static final String ERROR = "error";
    public static final String NOTIFICATION = "io.bettergram.service.MailChimpService";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public MailChimpService() {
        super("MailChimpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final boolean subscribeNewsletter = intent.getBooleanExtra(EXTRA_SUBSCRIBE_NEWSLETTER, false);
        final String email = intent.getStringExtra(EXTRA_SUBSCRIBE_EMAIL);

        final List<String> listIds = new ArrayList<String>() {
            {
                add(BuildConfig.MAILCHIMP_BASE_LIST_ID);
                if (subscribeNewsletter) {
                    add(BuildConfig.MAILCHIMP_NEWSLETTER_LIST_ID);
                }
            }
        };


        OkHttpClient client = new OkHttpClient();
        String credentials = "anystring:" + BuildConfig.MAILCHIMP_API_KEY;
        String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        RequestBody body = RequestBody.create(JSON, "{\n" +
                "    \"email_address\": \"" + email + "\",\n" +
                "    \"status\": \"subscribed\"\n" +
                "}");

        for (int i = 0, size = listIds.size(); i < size; i++) {
            Request request = new Request.Builder()
                    .url(BuildConfig.MAILCHIMP_BASE_URL + "/lists/" + listIds.get(i) + "/members")
                    .post(body)
                    .addHeader("Authorization", basicAuth)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.body() != null) {
                    String json = response.body().string();
                    if (response.isSuccessful()) {
                        if (i == size - 1) {
                            publishResults(json, NOTIFICATION, RESULT);
                        }
                    } else {
                        if (response.code() == 400) {
                            try {
                                JSONObject error = new JSONObject(json);
                                String msg = error.getString("title");
                                if (!isEmpty(msg) && msg.equals("Member Exists")) {
                                    Map<String, String> errors = new HashMap<>();
                                    errors.put("Member Exists", email + " is already a list member.");
                                    publishResults(new JSONObject(errors).toString(), NOTIFICATION, ERROR);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
