package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class News {

  @JsonField(fieldName = "source")
  public Source source;

  @JsonField(fieldName = "title")
  public String title;

  @JsonField(fieldName = "url")
  public String url;

  @JsonField(fieldName = "urlToImage")
  public String urlToImage;

  @JsonField(fieldName = "publishedAt")
  public String publishedAt;

}
