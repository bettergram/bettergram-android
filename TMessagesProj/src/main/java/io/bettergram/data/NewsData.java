package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;

@JsonType
public class NewsData {

  @JsonField(fieldName = "success")
  public boolean success;

  @JsonField(fieldName = "news")
  public List<String> news;
}
