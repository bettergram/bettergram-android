package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.Date;

import io.bettergram.utils.HttpDate;

@JsonType
public class News {

    @JsonField(fieldName = "source")
    public Source source;

    @JsonField(fieldName = "title")
    public String title;

    @JsonField(fieldName = "url")
    public String url;

    @JsonField(fieldName = "urlToImage")
    public String urlToImage;

    @JsonField(fieldName = "publishedAt")
    public String publishedAt;

    public Date getPublishedAt() {
        try {
            //return FROM_FORMAT2.parse(publishedAt);
            return HttpDate.parse(publishedAt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
