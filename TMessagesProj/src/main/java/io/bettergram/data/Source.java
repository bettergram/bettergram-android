package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType
public class Source {

  @JsonField(fieldName = "id")
  public String id;

  @JsonField(fieldName = "name")
  public String name;

}