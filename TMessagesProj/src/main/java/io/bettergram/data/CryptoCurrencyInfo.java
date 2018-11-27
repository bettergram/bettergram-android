package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class CryptoCurrencyInfo {
    @JsonField(fieldName = "name")
    public String name;
    @JsonField(fieldName = "code")
    public String code;
    @JsonField(fieldName = "volume")
    public double volume;
    @JsonField(fieldName = "cap")
    public double cap;
    @JsonField(fieldName = "rank")
    public int rank;
    @JsonField(fieldName = "price")
    public double price;
    @JsonField(fieldName = "delta")
    public CryptoCurrencyInfoDelta delta;
//    @JsonField(fieldName = "icon")
//    public String icon;
    @JsonField(fieldName = "supply")
    public long supply;
    @JsonField(fieldName = "circulating")
    public long circulating;
    @JsonField(fieldName = "favorite")
    public boolean favorite;

    public String getIcon() {
        return "https://beta.livecoinwatch.com/public/coins/icons/32/" + code.toLowerCase() + ".png";
    }
}
