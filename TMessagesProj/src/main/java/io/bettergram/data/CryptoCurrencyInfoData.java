package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.List;

@JsonType
public class CryptoCurrencyInfoData {
    @JsonField(fieldName = "list")
    public List<CryptoCurrencyInfo> list;
    @JsonField(fieldName = "favorites")
    public List<CryptoCurrencyInfo> favorites;
}
