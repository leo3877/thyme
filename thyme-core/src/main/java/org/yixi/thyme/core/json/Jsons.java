package org.yixi.thyme.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.util.List;
import org.yixi.thyme.core.Thyme;

/**
 * JSON 工具类。
 *
 * @author yixi
 * @since 1.0.0
 */
public abstract class Jsons {

  private static final Json json;

  static {
    json = new Json();
  }

  public static String encode(Object obj) {
    return json.encode(obj);
  }

  public static String encode(Object obj, String defaultValue) {
    if (obj == null) {
      return defaultValue;
    }
    return encode(obj);
  }

  public static String encodePretty(Object obj) {
    return json.encodePretty(obj);
  }

  public static <T> T decode(String jsonString, TypeReference<T> type) {
    return json.decode(jsonString, type);
  }

  public static <T> T decode(String jsonString, Class<T> type) {
    return json.decode(jsonString, type);
  }

  public static <T> List<T> decodeList(String jsonString, Class<T> type) {
    return json.decodeList(jsonString, type);
  }

  public static JsonNode toJsonNode(Object value) {
    return json.toJsonNode(value);
  }

  /**
   * Thyme Jackson ObjectMapper。
   *
   * @author yixi
   * @since 1.0.0
   */
  public static class ThymeObjectMapper {

    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

    static {
      /**
       * 下划线转驼峰
       */
//      defaultObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      defaultObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
      defaultObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      defaultObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
      defaultObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      defaultObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
      /**
       * 忽略空字段
       */
      defaultObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      /**
       * GMT+8 时区
       */
      SimpleDateFormat dateFormat = new SimpleDateFormat(Thyme.DATE_TIME_FORMAT);
      dateFormat.setTimeZone(Thyme.GMT8_TIME_ZONE);
      defaultObjectMapper.setDateFormat(dateFormat);
      defaultObjectMapper.registerModule(new JavaTimeModule());
    }

    public static ObjectMapper defaultObjectMapper() {
      return defaultObjectMapper;
    }

    public static ObjectMapper copy() {
      return defaultObjectMapper.copy();
    }

  }
}
