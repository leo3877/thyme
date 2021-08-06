package org.yixi.thyme.core;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.yixi.thyme.core.json.Jsons;

/**
 * 数据库实体对象,映射任意数据结构, 当数据源是 schema-free 结构时使用, 如: mongo
 *
 * @author yixi
 * @since 1.0.0
 */
public class EntityMap<P> extends BaseEntity<P> implements Map {

  private Map data = new LinkedHashMap();

  public EntityMap() {

  }

  @JsonCreator
  public EntityMap(Map<String, Object> map) {
    this.putAll(map);
  }

  @Override
  public P getId() {
    return (P) data.get("id");
  }

  @Override
  public void setId(P id) {
    data.put("id", id);
  }

  @Override
  public Date getCreateTime() {
    return (Date) data.get(CREATE_TIME);
  }

  @Override
  public void setCreateTime(Date createTime) {
    data.put(CREATE_TIME, createTime);
  }

  @Override
  public Date getUpdateTime() {
    return (Date) data.get(UPDATE_TIME);
  }

  @Override
  public void setUpdateTime(Date updateTime) {
    data.put(UPDATE_TIME, updateTime);
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return data.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return data.containsKey(value);
  }

  @Override
  public Object get(Object key) {
    return data.get(key);
  }

  @JsonAnySetter
  @Override
  public Object put(Object key, Object value) {
    if (CREATE_TIME.equals(key) || UPDATE_TIME.equals(key)) {
      if (value instanceof String) {
        return data.put(key, Jsons.decode((String) value, Date.class));
      } else if (value != null && !(value instanceof Date)) {
        throw new IllegalArgumentException("Type is invalid, must be Date." + value);
      }
    }
    return data.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return data.remove(key);
  }

  @Override
  public void putAll(Map map) {
    Object createdDate = map.get(CREATE_TIME);
    if (createdDate instanceof String) {
      map.put(CREATE_TIME, Jsons.decode((String) createdDate, Date.class));
    } else if (createdDate != null && !(createdDate instanceof Date)) {
      throw new IllegalArgumentException("Type is invalid, must be Date." + createdDate);
    }
    Object updatedDate = map.get(UPDATE_TIME);
    if (updatedDate instanceof String) {
      map.put(UPDATE_TIME, Jsons.decode((String) updatedDate, Date.class));
    } else if (updatedDate != null && !(updatedDate instanceof Date)) {
      throw new IllegalArgumentException("Type is invalid, must be Date." + updatedDate);
    }
    data.putAll(map);
  }

  @Override
  public void clear() {
    data.clear();
  }

  @Override
  public Set keySet() {
    return data.keySet();
  }

  @Override
  public Collection values() {
    return data.values();
  }

  @Override
  public Set<Entry> entrySet() {
    return data.entrySet();
  }

  @JsonValue
  private Map toJson() {
    return data;
  }

  @Override
  public String toString() {
    return data.toString();
  }

}
