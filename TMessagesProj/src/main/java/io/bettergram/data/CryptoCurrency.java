package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class CryptoCurrency {
    @JsonField(fieldName = "code")
    public String code;
    @JsonField(fieldName = "name")
    public String name;
    @JsonField(fieldName = "url")
    public String url;
    @JsonField(fieldName = "icon")
    public String icon;
}
