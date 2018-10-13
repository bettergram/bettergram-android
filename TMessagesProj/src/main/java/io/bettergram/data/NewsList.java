package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;

@JsonType
public class NewsList {

  @JsonField(fieldName = "articles")
  public List<News> articles;

}
