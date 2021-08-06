package org.yixi.data.client;

import com.google.common.collect.Lists;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.ex.ThymeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yixi
 * @since 1.0.0
 */
public class RestFilter {

  protected Map<String, Object> filter;
  protected String currentKey;
  protected boolean hasNot;

  public static RestFilter create() {
    return new RestFilter();
  }

  public static RestFilter create(Map<String, Object> filter) {
    return new RestFilter(filter);
  }

  private RestFilter() {
    filter = new Document<>();
  }

  private RestFilter(Map<String, Object> filter) {
    if (filter != null && !filter.isEmpty()) {
      this.filter = new LinkedHashMap<>(filter);
      adjustDateType(this.filter);
    } else {
      this.filter = new LinkedHashMap<>();
    }
  }

  /**
   * Compound filter chains to be more readable. RestFilter.instance().key("age").gt(18).lt(45).key("str").eq(3)
   */
  public RestFilter key(final String key) {
    currentKey = key;
    if (filter.get(key) == null) {
      filter.put(currentKey, new NullObject());
    }
    return this;
  }

  public RestFilter gt(final Object object) {
    addOperand("gt", object);
    return this;
  }

  public RestFilter gte(final Object object) {
    addOperand("gte", object);
    return this;
  }

  public RestFilter lt(final Object object) {
    addOperand("lt", object);
    return this;
  }

  public RestFilter lte(final Object object) {
    addOperand("lte", object);
    return this;
  }

  public RestFilter eq(final Object object) {
    addOperand(null, object);
    return this;
  }

  public RestFilter ne(final Object object) {
    addOperand("ne", object);
    return this;
  }

  public RestFilter in(final List<Object> objects) {
    List l = (List) filter.get("__or");
    if (l == null) {
      l = new ArrayList();
      filter.put("__or", l);
    }
    for (Object object : objects) {
      l.add(new Document<>(currentKey, object));
    }
    filter.remove(currentKey);
    return this;
  }

  public RestFilter notIn(final List<Object> objects) {
    addOperand("nin", objects);
    return this;
  }

  public RestFilter regex(final String regex) {
    addOperand("regex", regex);
    return this;
  }

  public RestFilter exists(final Object object) {
    addOperand("exists", object);
    return this;
  }

  public RestFilter anyOf(final List items) {
    addOperand("anyOf", items);
    return this;
  }

  public RestFilter allOf(final List items) {
    addOperand("allOf", items);
    return this;
  }

  public RestFilter notOf(final List items) {
    addOperand("notOf", items);
    return this;
  }

  public RestFilter contains(final Object object) {
    addOperand("contains", object);
    return this;
  }

  public RestFilter startsWith(final Object object) {
    addOperand("startsWith", object);
    return this;
  }

  public RestFilter endsWith(final Object object) {
    addOperand("endsWith", object);
    return this;
  }

  public RestFilter nearSphere(double longitude, double latitude) {
    nearSphere(longitude, latitude, null);
    return this;
  }

  public RestFilter nearSphere(double longitude, double latitude, Integer maxDistance) {
    nearSphere(longitude, latitude, null, maxDistance);
    return this;
  }

  public RestFilter nearSphere(double longitude, double latitude, Integer minDistance,
    Integer maxDistance) {
    Map nearSphere = new HashMap();
    nearSphere.put("coordinates", Lists.newArrayList(longitude, latitude));
    if (minDistance != null) {
      nearSphere.put("minDistance", minDistance);
    }
    if (maxDistance != null) {
      nearSphere.put("maxDistance", maxDistance);
    }
    addOperand("nearSphere", nearSphere);
    return this;
  }

  public RestFilter not() {
    hasNot = true;
    return this;
  }

  /**
   * Equivalent to an or operand
   */
  public RestFilter or(final RestFilter... ors) {
    List l = (List) filter.get("__or");
    if (l == null) {
      l = new ArrayList();
      filter.put("__or", l);
    }
    for (RestFilter f : ors) {
      l.add(f.filter());
    }
    return this;
  }

  public RestFilter and(final RestFilter... ands) {
    List l = (List) filter.get("__and");
    if (l == null) {
      l = new ArrayList();
      filter.put("__and", l);
    }
    for (RestFilter f : ands) {
      l.add(f.filter());
    }
    return this;
  }

  public Map<String, Object> filter() {
    for (final String key : filter.keySet()) {
      if (filter.get(key) instanceof NullObject) {
        throw new ThymeException("No operand for key:" + key);
      }
    }
    adjustDateType(filter);
    return filter;
  }

  protected void addOperand(final String op, final Object value) {
    Object valueToPut = value;
    if (op == null) {
      if (hasNot) {
        valueToPut = new Document<>("__not", valueToPut);
        hasNot = false;
      }
      filter.put(currentKey, valueToPut);
      return;
    }

    Object storedValue = filter.get(currentKey);
    Document<String, Object> operand;
    if (!(storedValue instanceof Document)) {
      operand = new Document<>();
      if (hasNot) {
        Document<String, Object> notOperand = new Document<>("__not", operand);
        filter.put(currentKey, notOperand);
        hasNot = false;
      } else {
        filter.put(currentKey, operand);
      }
    } else {
      operand = (Document<String, Object>) filter.get(currentKey);
      if (operand.get("__not") != null) {
        operand = (Document<String, Object>) operand.get("__not");
      }
    }

    operand.put(op, valueToPut);
  }

  private void adjustDateType(Map<String, Object> map) {
    map.forEach((k, v) -> {
      if (v instanceof List) {
        map.put(k, adjustDateType((List) v));
      } else if (v instanceof Map) {
        adjustDateType((Map) v);
      } else if (v instanceof Date) {
        Map date = new HashMap();
        date.put("__date", v);
        map.put(k, date);
      }
    });
  }

  private List<Object> adjustDateType(List<Object> objects) {
    List<Object> newList = new ArrayList<>();
    for (Object object : objects) {
      if (object instanceof Map) {
        adjustDateType((Map) object);
        newList.add(object);
      } else if (object instanceof List) {
        newList.add(adjustDateType((List) object));
      } else if (object instanceof Date) {
        Map date = new HashMap();
        date.put("__date", object);
        newList.add(date);
      } else {
        newList.add(object);
      }
    }
    return newList;
  }


  /**
   * @author yixi
   */
  private static class NullObject {

  }
}
