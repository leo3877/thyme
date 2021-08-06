package org.yixi.data.client;

import org.yixi.thyme.core.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author yixi
 * @since 1.0.1
 */
public class TypeConverters {

  private final static TypeConverter<Date, Map> dateTypeConverter = new DateTypeConverter();


  public TypeConverters() {
  }

  public static Object convert(Object item) {
    if (item instanceof Date) {
      return dateTypeConverter.convert((Date) item);
    } else if (item instanceof Map) {
      return recursive((Map) item);
    } else if (item instanceof List) {
      return recursive((List) item);
    } else {
      return item;
    }
  }

  static Map recursive(Map<String, Object> map) {
    map.forEach((k, v) -> {
      if (v instanceof List) {
        map.put(k, recursive((List) v));
      } else if (v instanceof Map) {
        recursive((Map<String, Object>) v);
      } else {
        map.put(k, convert(v));
      }
    });
    return map;
  }

  static List<Object> recursive(List<Object> objects) {
    List<Object> newList = new ArrayList<>();
    for (Object object : objects) {
      if (object instanceof Map) {
        recursive((Map<String, Object>) object);
        newList.add(object);
      } else if (object instanceof List) {
        newList.add(recursive((List) object));
      } else {
        newList.add(convert(object));
      }
    }
    return newList;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class DateTypeConverter implements TypeConverter<Date, Map> {

    @Override
    public Map convert(Date item) {
      return new Document("__date", item);
    }
  }
}
