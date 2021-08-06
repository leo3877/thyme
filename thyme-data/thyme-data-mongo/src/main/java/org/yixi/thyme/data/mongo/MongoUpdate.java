package org.yixi.thyme.data.mongo;

import org.yixi.thyme.core.ex.ThymeException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mongo 更新对象。
 *
 * @author yixi
 * @since 1.0.0
 */
public class MongoUpdate {

  /**
   * Element position.
   */
  public enum Position {
    LAST, FIRST
  }

  protected Map<String, Map<String, Object>> modifierOps = new LinkedHashMap<>();
  protected boolean upsert;

  public MongoUpdate() {
  }

  public MongoUpdate(Map<String, Map<String, Object>> values) {
    this.modifierOps.putAll(values);
  }

  public static MongoUpdate create() {
    return new MongoUpdate();
  }

  public static MongoUpdate create(Map<String, Map<String, Object>> values) {
    return new MongoUpdate(values);
  }

  /**
   * Example:
   * <pre>
   *     Map&lt;String, Object&gt; a = new HashMap&lt;&gt;();
   *     a.put("c1", "val1")
   *
   *     setCascade("pro", a);  // 解析成 mongo 更新语法是: { $set: { "pro.c1": 'val1' } }
   *
   *     $set("pro", a); // 解释成 mongo 更新语法是: { $set: { "pro": { c1: 'val1' } } }
   * </pre>
   */
  public MongoUpdate setDeep(String key, Object value) {
    if (value instanceof Map) {
      for (Object childKey : ((Map) value).keySet()) {
        setDeep(key + "." + childKey, ((Map) value).get(childKey));
      }
    } else {
      addMultiFieldOperation("$set", key, value);
    }
    return this;
  }

  public MongoUpdate setDeep(Map<String, Object> values) {
    if (values != null) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        setDeep(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }


  public MongoUpdate set(String key, Object value) {
    addMultiFieldOperation("$set", key, value);
    return this;
  }

  public MongoUpdate set(Map<String, Object> values) {
    if (values != null) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {
        set(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }


  public MongoUpdate setOnInsert(String key, Object value) {
    if ("id".equals(key)) {
      addMultiFieldOperation("$setOnInsert", "_id", value);
    } else {
      addMultiFieldOperation("$setOnInsert", key, value);
    }
    return this;
  }

  public MongoUpdate unset(String key) {
    addMultiFieldOperation("$unset", key, 1);
    return this;
  }

  public MongoUpdate unset(List<String> keys) {
    if (keys != null) {
      for (String key : keys) {
        unset(key);
      }
    }
    return this;
  }

  public MongoUpdate inc(String key, Number inc) {
    addMultiFieldOperation("$inc", key, inc);
    return this;
  }

  public MongoUpdate push(String key, List<Object> values) {
    Map<String, Object> each = new LinkedHashMap<>();
    each.put("$each", values);
    addMultiFieldOperation("$push", key, each);
    return this;
  }

  public MongoUpdate push(String key, Object value) {
    addMultiFieldOperation("$push", key, value);
    return this;
  }

  public MongoUpdate addToSet(String key, Object value) {
    addMultiFieldOperation("$addToSet", key, value);
    return this;
  }

  public MongoUpdate addToSet(String key, List<Object> values) {
    Map<String, Object> each = new LinkedHashMap<>();
    each.put("$each", values);
    addMultiFieldOperation("$addToSet", key, each);
    return this;
  }

  public MongoUpdate pop(String key, Position pos) {
    addMultiFieldOperation("$pop", key, pos == Position.FIRST ? -1 : 1);
    return this;
  }

  public MongoUpdate pull(String key, Object value) {
    addMultiFieldOperation("$pull", key, value);
    return this;
  }

  public MongoUpdate pullAll(String key, List<Object> values) {
    addMultiFieldOperation("$pullAll", key, values);
    return this;
  }

  public MongoUpdate rename(String oldName, String newName) {
    addMultiFieldOperation("$rename", oldName, newName);
    return this;
  }

  protected void addMultiFieldOperation(String operator, String key, Object value) {
    Object existingValue = modifierOps.get(operator);
    Map<String, Object> keyValueMap;

    if (existingValue == null) {
      keyValueMap = new LinkedHashMap<>();
      modifierOps.put(operator, keyValueMap);
    } else if (existingValue instanceof LinkedHashMap) {
      @SuppressWarnings("unchecked")
      Map<String, Object> kv = (Map<String, Object>) existingValue;
      keyValueMap = kv;
    } else {
      throw new ThymeException("Modifier Operations should be a LinkedHashMap but was "
        + existingValue.getClass());
    }
    keyValueMap.put(key, value);
  }

  public boolean isUpsert() {
    return upsert;
  }

  public MongoUpdate setUpsert(boolean upsert) {
    this.upsert = upsert;
    return this;
  }

  public Map<String, Map<String, Object>> getUpdateDoc() {
    return modifierOps;
  }

  @Override
  public String toString() {
    return "MongoUpdate{" +
      "modifierOps=" + modifierOps +
      ", upsert=" + upsert +
      '}';
  }
}

