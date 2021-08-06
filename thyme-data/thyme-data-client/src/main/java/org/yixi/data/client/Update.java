package org.yixi.data.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author yixi
 * @since 1.1.2
 */
public class Update implements Serializable {

  public Operators operators = new Operators();

  private boolean upsert;

  private Map<String, Object> setOnInserts;


  public static Update create() {
    return new Update();
  }

  public Update setUpsert(boolean upsert) {
    this.upsert = upsert;
    return this;
  }

  public Update setSetOnInserts(Map<String, Object> setOnInserts) {
    this.setOnInserts = setOnInserts;
    return this;
  }

  public Update set(Map<String, Object> sets) {
    if (operators.getSet() == null) {
      operators.setSet(new HashMap<>());
    }
    operators.getSet().putAll(sets);
    return this;
  }

  public Update set(String key, Object val) {
    if (operators.getSet() == null) {
      operators.setSet(new HashMap<>());
    }
    operators.getSet().put(key, val);
    return this;
  }

  public Update unset(String... keys) {
    if (operators.getUnset() == null) {
      operators.setUnset(new ArrayList<>());
    }
    for (String key : keys) {
      operators.getUnset().add(key);
    }
    return this;
  }

  public Update unset(List keys) {
    if (operators.getUnset() == null) {
      operators.setUnset(new ArrayList<>());
    }
    operators.getUnset().addAll(keys);
    return this;
  }

  public Update inc(String key, Integer delta) {
    if (operators.getInc() == null) {
      operators.setInc(new HashMap<>());
    }
    operators.getInc().put(key, delta);
    return this;
  }

  public Update push(String key, Object val) {
    if (operators.getPush() == null) {
      operators.setPush(new HashMap<>());
    }
    List<Object> objects = operators.getPush().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPush().put(key, objects);
    }
    objects.add(val);
    return this;
  }

  public Update push(String key, List<Object> vals) {
    if (operators.getPush() == null) {
      operators.setPush(new HashMap<>());
    }
    List<Object> objects = operators.getPush().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPush().put(key, objects);
    }
    objects.addAll(vals);
    return this;
  }

  public Update pushUnique(String key, Object val) {
    if (operators.getPushUnique() == null) {
      operators.setPushUnique(new HashMap<>());
    }
    List<Object> objects = operators.getPushUnique().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPushUnique().put(key, objects);
    }
    objects.add(val);
    return this;
  }

  public Update pushUnique(String key, List<Object> vals) {
    if (operators.getPushUnique() == null) {
      operators.setPushUnique(new HashMap<>());
    }
    List<Object> objects = operators.getPushUnique().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPushUnique().put(key, objects);
    }
    objects.addAll(vals);
    return this;
  }

  public Update pull(String key, Object val) {
    if (operators.getPull() == null) {
      operators.setPull(new HashMap<>());
    }
    List<Object> objects = operators.getPull().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPull().put(key, objects);
    }
    objects.add(val);
    return this;
  }

  public Update pull(String key, List<Object> vals) {
    if (operators.getPull() == null) {
      operators.setPull(new HashMap<>());
    }
    List<Object> objects = operators.getPull().get(key);
    if (objects == null) {
      objects = new ArrayList<>();
      operators.getPull().put(key, objects);
    }
    objects.addAll(vals);
    return this;
  }

  public Operators getOperators() {
    return operators;
  }

  public Map<String, Object> getSetOnInserts() {
    return setOnInserts;
  }

  public boolean isUpsert() {
    return upsert;
  }

  /**
   * @author yixi
   */
  @Data
  public static class Operators {

    private Map<String, Object> set;
    private List<String> unset;
    private Map<String, Integer> inc;
    private Map<String, List<Object>> push;
    private Map<String, List<Object>> pushUnique;
    private Map<String, List<Object>> pull;
  }

}
