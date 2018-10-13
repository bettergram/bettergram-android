package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class ResourceItem {
    @JsonField(fieldName = "title")
    public String title;
    @JsonField(fieldName = "description")
    public String description;
    @JsonField(fieldName = "url")
    public String url;
    @JsonField(fieldName = "iconUrl")
    public String iconUrl;

    public String thumbnail() {
        return iconUrl.replace("s=460&v=4", "s=84&v=4");
    }
}
