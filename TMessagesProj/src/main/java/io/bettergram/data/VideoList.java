package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;

@JsonType
public class VideoList {

  @JsonField(fieldName = "videos")
  public List<Video> videos;

}
