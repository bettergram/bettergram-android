package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.List;

@JsonType
public class ResourceGroup {
    @JsonField(fieldName = "title")
    public String title;
    @JsonField(fieldName = "items")
    public List<ResourceItem> items;
}
