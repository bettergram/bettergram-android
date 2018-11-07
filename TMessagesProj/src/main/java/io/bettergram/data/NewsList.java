package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.Collections;
import java.util.List;

@JsonType
public class NewsList {

    @JsonField(fieldName = "articles")
    public List<News> articles;

    /**
     * Sorts article list by published date
     *
     * @return
     */
    public void sortArticlesByDate() {
        Collections.sort(articles, (o1, o2) -> (o2.getPublishedAt() == null || o1.getPublishedAt() == null) ? 0 : o2.getPublishedAt().compareTo(o1.getPublishedAt()));
    }
}
