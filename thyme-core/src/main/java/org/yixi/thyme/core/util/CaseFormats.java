package org.yixi.thyme.core.util;

import com.google.common.base.CaseFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author yixi
 * @since 1.1.3
 */
public class CaseFormats {

  public static void lowerToUnderscore(Map<String, Object> map) {
    for (String key : new HashSet<>(map.keySet())) {
      Object val = map.get(key);
      if (val instanceof List) {
        lowerToUnderscore((List<Object>) val);
      }
      String target = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
      if (!target.equals(key)) {
        map.put(target, val);
        map.remove(key);
      }
    }
  }

  public static void lowerToUnderscore(List<Object> objects) {
    for (Object obj : objects) {
      if (obj instanceof Map) {
        lowerToUnderscore((Map<String, Object>) obj);
      }
    }
  }
}
