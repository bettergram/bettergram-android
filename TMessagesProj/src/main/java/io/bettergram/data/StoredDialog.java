package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class StoredDialog {
    @JsonField(fieldName = "did")
    public long did;
    @JsonField(fieldName = "pinned_num")
    public int pinned_num;
    @JsonField(fieldName = "favorited_date")
    public int favorited_date;
}
