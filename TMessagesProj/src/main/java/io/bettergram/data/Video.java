package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class Video {

  @JsonField(fieldName = "id")
  public String id;

  @JsonField(fieldName = "channelTitle")
  public String channelTitle;

  @JsonField(fieldName = "title")
  public String title;

  @JsonField(fieldName = "viewCount")
  public String viewCount;

  @JsonField(fieldName = "duration")
  public String duration;

  @JsonField(fieldName = "publishedAt")
  public String publishedAt;
}
