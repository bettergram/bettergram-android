package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class CryptoCurrencyInfoDelta {
    @JsonField(fieldName = "second")
    public double second;
    @JsonField(fieldName = "minute")
    public double minute;
    @JsonField(fieldName = "hour")
    public double hour;
    @JsonField(fieldName = "day")
    public double day;
    @JsonField(fieldName = "week")
    public double week;
    @JsonField(fieldName = "month")
    public double month;
}
