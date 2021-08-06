package org.yixi.thyme.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.yixi.thyme.core.Thyme;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类。
 *
 * @author yixi
 * @since 1.0.0
 */
public abstract class ThymeJsons {

  private static final TypeConverter dateTypeConverter = new DateTypeConverter();
  private static final Json json;

  static {
    ObjectMapper mapper = Jsons.ThymeObjectMapper.copy();
    SimpleModule module = new SimpleModule();
    module.addSerializer(Date.class, new DateSerializer());
    module.addDeserializer(Date.class, new DateDeserializer());
    mapper.registerModule(module);
    json = new Json(mapper);
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
    T decode = json.decode(jsonString, type);
    if (decode instanceof Map) {
      convert((Map<String, Object>) decode);
    }
    return decode;
  }

  public static <T> T decode(String jsonString, Class<T> type) {
    T decode = json.decode(jsonString, type);
    if (decode instanceof Map) {
      convert((Map<String, Object>) decode);
    }
    return decode;
  }

  public static <T> List<T> decodeList(String jsonString, Class<T> type) {
    List<T> list = json.decodeList(jsonString, type);
    if (list != null && list.size() > 0 && list.get(0) instanceof Map) {
      convert((List<Object>) list);
    }
    return list;
  }

  public static JsonNode toJsonNode(Object value) {
    return json.toJsonNode(value);
  }

  /**
   * @author yixi
   */
  public static class DateSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date date, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
      jgen.writeStartObject();
      jgen.writeStringField("__date", Thyme.DATE_TIME_FORMAT_GMT_8.format(date));
      jgen.writeEndObject();
    }
  }

  /**
   * @author yixi
   */
  public static class DateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      String str = node.get("__date").asText();
      return Thyme.dateParse(str);
    }
  }

  public static void convert(Map<String, Object> map) {
    if (map == null) {
      return;
    }
    map.forEach((k, v) -> {
      if (v instanceof List) {
        map.put(k, convert((List) v));
      } else if (v instanceof Map) {
        boolean recursive = true;
        Object res = dateTypeConverter.convert((Map) v);
        if (res instanceof Date) {
          map.put(k, res);
          recursive = false;
        }
        if (recursive) {
          convert((Map) v);
        }
      }
    });
  }

  public static List<Object> convert(List<Object> objects) {
    if (objects == null || objects.isEmpty()) {
      return objects;
    }
    List<Object> newList = new ArrayList<>();
    for (Object object : objects) {
      if (object instanceof Map) {
        convert((Map<String, Object>) object);
        newList.add(object);
      } else if (object instanceof List) {
        newList.add(convert((List) object));
      } else {
        newList.add(object);
      }
    }
    return newList;
  }

  /**
   * @author yixi
   * @since 1.1.2
   */
  public interface TypeConverter {

    Object convert(Map map);
  }

  /**
   * @author yixi
   * @since 1.1.2
   */
  public static class DateTypeConverter implements TypeConverter {

    @Override
    public Object convert(Map data) {
      Object date = data.get("__date");
      if (date != null) {
        try {
          return Thyme.DATE_TIME_FORMAT_GMT_8.parse(date.toString());
        } catch (Exception e) {
          throw Thyme.ex("日期格式必须是：%s, date: %s", Thyme.DATE_TIME_FORMAT, date);
        }
      } else {
        return null;
      }
    }
  }
}
