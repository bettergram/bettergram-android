package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class CryptoCurrencyInfoResponse {
    @JsonField(fieldName = "success")
    public boolean success;
    @JsonField(fieldName = "cap")
    public double cap;
    @JsonField(fieldName = "volume")
    public double volume;
    @JsonField(fieldName = "btcDominance")
    public double btcDominance;
    @JsonField(fieldName = "sort")
    public String sort;
    @JsonField(fieldName = "order")
    public String order;
    @JsonField(fieldName = "offset")
    public int offset;
    @JsonField(fieldName = "limit")
    public int limit;
    @JsonField(fieldName = "currency")
    public String currency;
    @JsonField(fieldName = "total")
    public int total;
    @JsonField(fieldName = "data")
    public CryptoCurrencyInfoData data;
}
