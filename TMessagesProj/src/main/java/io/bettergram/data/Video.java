package io.bettergram.data;

import com.crashlytics.android.Crashlytics;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.text.ParseException;
import java.util.Date;

import static io.bettergram.service.api.VideosApi.FROM_FORMAT2;

@JsonType
public class Video {

    @JsonField(fieldName = "id")
    public String id;

    @JsonField(fieldName = "channelTitle")
    public String channelTitle;

    @JsonField(fieldName = "title")
    public String title;

    @JsonField(fieldName = "viewCount")
    public String viewCount;

    @JsonField(fieldName = "duration")
    public String duration;

    @JsonField(fieldName = "publishedAt")
    public String publishedAt;

    public Date getPublishedAt() {
        try {
            return FROM_FORMAT2.parse(publishedAt);
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }
    }
}
