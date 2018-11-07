package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.text.ParseException;
import java.util.Date;

import static io.bettergram.service.api.NewsApi.FROM_FORMAT2;

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
            return FROM_FORMAT2.parse(publishedAt);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
