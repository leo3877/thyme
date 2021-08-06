package org.yixi.thyme.core.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons.ThymeObjectMapper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yixi
 * @since 1.1.0
 */
public class Json {

  private final ObjectMapper objectMapper;

  public Json() {
    this(ThymeObjectMapper.defaultObjectMapper());
  }

  public Json(ObjectMapper mapper) {
    this.objectMapper = mapper;
  }

  public String encode(Object obj) {
    try {
      if (obj == null) {
        return null;
      }
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public String encode(Object obj, String defaultValue) {
    if (obj == null) {
      return defaultValue;
    }
    return encode(obj);
  }

  public String encodePretty(Object obj) {
    try {
      if (obj == null) {
        return null;
      }
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public <T> T decode(String jsonString, TypeReference<T> type) {
    if (StringUtils.isEmpty(jsonString)) {
      return null;
    }
    try {
      return objectMapper.readValue(jsonString, type);
    } catch (Exception e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public <T> T decode(String jsonString, Class<T> type) {
    if (type == Date.class) {
      try {
        return (T) Thyme.DATE_TIME_FORMAT_GMT_8.parse(jsonString);
      } catch (ParseException e) {
        throw new ThymeException(
          String
            .format("Invalid datetime format, must be : %s dateString: %s", Thyme.DATE_TIME_FORMAT,
              jsonString));
      }
    } else if (StringUtils.isEmpty(jsonString)) {
      return null;
    }
    try {
      return objectMapper.readValue(jsonString, type);
    } catch (Exception e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public <T> List<T> decodeList(String jsonString, Class<T> type) {
    if (StringUtils.isEmpty(jsonString)) {
      return new ArrayList();
    }
    try {
      return objectMapper.readValue(jsonString,
        objectMapper.getTypeFactory().constructCollectionType(List.class, type));
    } catch (Exception e) {
      throw new ThymeException(e.getMessage());
    }
  }

  public JsonNode toJsonNode(Object value) {
    if (value instanceof JsonNode) {
      return (JsonNode) value;
    }
    return objectMapper.valueToTree(value);
  }

}
