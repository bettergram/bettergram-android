package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class ResourcesData {
    @JsonField(fieldName = "success")
    public boolean success;
    @JsonField(fieldName = "resources")
    public Resources resources;
}
