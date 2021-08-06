package org.yixi.thyme.data.mysql;

import java.util.Arrays;
import java.util.List;

/**
 * @author yixi
 * @since 1.0.0
 */
public abstract class MysqlConditions {

  public static And and(Condition... conditions) {
    return new And(Arrays.asList(conditions));
  }

  public static Or or(Condition... conditions) {
    return new Or(Arrays.asList(conditions));
  }

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

  public static In in(String key, Object... values) {
    return new In(key, Arrays.asList(values));
  }

  public static NotIn notIn(String key, List<Object> values) {
    return new NotIn(key, values);
  }

  public static NotIn notIn(String key, Object... values) {
    return new NotIn(key, Arrays.asList(values));
  }

  public static Exists exists(String key, boolean value) {
    return new Exists(key, value);
  }

  public static Regex regex(String key, String value) {
    return new Regex(key, value);
  }

  public interface Condition {

  }

  /**
   * @author yixi
   */
  public static abstract class Condition0 implements Condition {

    private final String key;
    private final Object value;

    public Condition0(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    public Object getValue() {
      return value;
    }

    public Object getKey() {
      return key;
    }
  }

  /**
   * @author yixi
   */
  public static class Eq extends Condition0 {

    public Eq(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Ne extends Condition0 {

    public Ne(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Gt extends Condition0 {

    public Gt(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Gte extends Condition0 {

    public Gte(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Lt extends Condition0 {

    public Lt(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Lte extends Condition0 {

    public Lte(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class In extends Condition0 {

    public In(String key, List<Object> values) {
      super(key, values);
    }
  }

  /**
   * @author yixi
   */
  public static class NotIn extends Condition0 {

    public NotIn(String key, Object value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Exists extends Condition0 {

    public Exists(String key, Boolean value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class Regex extends Condition0 {

    public Regex(String key, String value) {
      super(key, value);
    }
  }

  /**
   * @author yixi
   */
  public static class And implements Condition {

    private List<Condition> conditions;

    public And(List<Condition> conditions) {
      this.conditions = conditions;
    }
  }

  /**
   * @author yixi
   */
  public static class Or implements Condition {

    private List<Condition> conditions;

    public Or(List<Condition> conditions) {
      this.conditions = conditions;
    }
  }

  /**
   * @author yixi
   */
  public static class Not implements Condition {

    private final Condition condition;

    public Not(Condition condition) {
      this.condition = condition;
    }
  }
}
