package org.yixi.thyme.data.mongo;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yixi
 * @since 1.0.0
 */
public class Conditions {

  public static Eq eq(String key, Object value) {
    return new Eq(key, value);
  }

  public static Ne ne(String key, Object value) {
    return new Ne(key, value);
  }

  public static Gt gt(String key, Object value) {
    return new Gt(key, value);
  }

  public static Gte gte(String key, Object value) {
    return new Gte(key, value);
  }

  public static Lt lt(String key, Object value) {
    return new Lt(key, value);
  }

  public static Lte lte(String key, Object value) {
    return new Lte(key, value);
  }

  public static In in(String key, List<Object> values) {
    return new In(key, values);
  }

  public static NotIn notIn(String key, List<Object> values) {
    return new NotIn(key, values);
  }

  public static Exists exists(String key, Boolean value) {
    return new Exists(key, value);
  }

  public static Regex regex(String key, String regex) {
    return new Regex(key, regex);
  }

  public static And and(Condition... conditions) {
    And and = new And();
    for (Condition condition : conditions) {
      and.addCondition(condition);
    }
    return and;
  }

  public static Or or(Condition... conditions) {
    Or or = new Or();
    for (Condition condition : conditions) {
      or.addCondition(condition);
    }
    return or;
  }

  /**
   * @author yixi
   */
  @Data
  @AllArgsConstructor
  public static class Condition {

  }

  /**
   * @author yixi
   */
  @Data
  @AllArgsConstructor
  public static class Value extends Condition {

    protected String key;
    protected Object value;
  }

  /**
   * @author yixi
   */
  public static class Logic extends Condition {

    protected List<Condition> conditions = new ArrayList<>();

    public void addCondition(Condition condition) {
      conditions.add(condition);
    }

    public List<Condition> getConditions() {
      return conditions;
    }
  }

  /**
   * @author yixi
   */
  public static class Eq extends Value {

    public Eq(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Ne extends Value {

    public Ne(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Gt extends Value {

    public Gt(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Gte extends Value {

    public Gte(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Lt extends Value {

    public Lt(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Lte extends Value {

    public Lte(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class In extends Value {

    public In(String key, List<Object> values) {
      super(key, values);
    }
  }

  /**
   * @author yixi
   */
  public static class NotIn extends Value {

    public NotIn(String key, List<Object> values) {
      super(key, values);
    }
  }

  /**
   * @author yixi
   */
  public static class Regex extends Value {

    public Regex(String key, String regex) {
      super(key, regex);
    }
  }

  /**
   * @author yixi
   */
  public static class Exists extends Value {

    public Exists(String key, Boolean exists) {
      super(key, exists);
    }
  }

  /**
   * @author yixi
   */
  public static class And extends Logic {

  }

  /**
   * @author yixi
   */
  public static class Or extends Logic {

  }
}
