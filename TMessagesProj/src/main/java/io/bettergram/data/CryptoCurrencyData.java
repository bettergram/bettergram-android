package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.List;

@JsonType
public class CryptoCurrencyData {
    @JsonField(fieldName = "success")
    public boolean success;
    @JsonField(fieldName = "coinsUrlBase")
    public String coinsUrlBase;
    @JsonField(fieldName = "coinsIcon32Base")
    public String coinsIcon32Base;
    @JsonField(fieldName = "coinsIcon64Base")
    public String coinsIcon64Base;
    @JsonField(fieldName = "data")
    public List<CryptoCurrency> data;
}
