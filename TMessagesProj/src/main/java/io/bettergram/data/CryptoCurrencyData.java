package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.List;

@JsonType
public class CryptoCurrencyData {
    @JsonField(fieldName = "success")
    public boolean success;
    @JsonField(fieldName = "data")
    public List<CryptoCurrency> data;
}
