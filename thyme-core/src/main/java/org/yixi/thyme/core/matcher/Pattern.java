package org.yixi.thyme.core.matcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yixi
 */
public class Pattern {

  private Operator operator; // or, and

  public static Pattern parse(Map dsl) {
    return PatternParser.fromDsl(dsl);
  }

  public static Pattern parse(Map dsl, boolean ignoreNull) {
    return PatternParser.fromDsl(dsl, ignoreNull);
  }

  public static Pattern parse(String operator, Object pattern) {
    return PatternParser.fromValue(operator, pattern);
  }

  public static Pattern parse(String operator, Object pattern, boolean ignoreNull) {
    return PatternParser.fromValue(operator, pattern, ignoreNull);
  }

  public static Pattern parse(Operator operator, Object pattern) {
    return PatternParser.fromValue(operator, pattern);
  }

  public static Pattern parse(Operator operator, Object pattern, boolean ignoreNull) {
    return PatternParser.fromValue(operator, pattern, ignoreNull);
  }

  public boolean match(Object target) {
    return Matchers.match(this, target);
  }

  public boolean lazyMatch(DataSource target) {
    return Matchers.match(this, target);
  }

  public Operator operator() {
    return operator;
  }

  protected void setOperator(Operator operator) {
    this.operator = operator;
  }

  /**
   * @author yixi
   */
  public static class Value extends Pattern {

    private final Object value;
    private final Boolean ignoreNull;

    public Value(Object value, Boolean ignoreNull) {
      this.value = value;
      this.ignoreNull = ignoreNull;
    }

    public Boolean ignoreNull() {
      return ignoreNull;
    }

    public Object value() {
      return value;
    }

    public boolean isString() {
      return value instanceof String;
    }

    public boolean isNumber() {
      return value instanceof Number;
    }

    public boolean isBoolean() {
      return value instanceof Boolean;
    }

    public boolean isArray() {
      return value instanceof List;
    }
  }

  /**
   * @author yixi
   */
  public static class Eq extends Value {

    public Eq(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.eq);
    }
  }

  /**
   * @author yixi
   */
  public static class Ne extends Value {

    public Ne(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.ne);
    }
  }

  /**
   * @author yixi
   */
  public static class Gte extends Value {

    public Gte(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.gte);
    }
  }

  /**
   * @author yixi
   */
  public static class Gt extends Value {

    public Gt(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.gt);
    }
  }

  /**
   * @author yixi
   */
  public static class Lte extends Value {

    public Lte(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.lte);
    }
  }

  /**
   * @author yixi
   */
  public static class Lt extends Value {

    public Lt(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.lt);
    }
  }

  /**
   * @author yixi
   */
  public static class In extends Value {

    public In(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.in);
    }
  }

  /**
   * @author yixi
   */
  public static class Nin extends Value {

    public Nin(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.nin);
    }
  }

  /**
   * @author yixi
   */
  public static class AnyOf extends Value {

    public AnyOf(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.anyOf);
    }
  }

  /**
   * @author yixi
   */
  public static class AllOf extends Value {

    public AllOf(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.allOf);
    }
  }

  /**
   * @author yixi
   */
  public static class NotOf extends Value {

    public NotOf(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.notOf);
    }
  }

  /**
   * @author yixi
   */
  public static class Contains extends Value {

    public Contains(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.contains);
    }
  }

  /**
   * @author yixi
   */
  public static class Regex extends Value {

    public Regex(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.regex);
    }
  }

  /**
   * @author yixi
   */
  public static class StartsWith extends Value {

    public StartsWith(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.startsWith);
    }
  }

  /**
   * @author yixi
   */
  public static class EndsWith extends Value {

    public EndsWith(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.endsWith);
    }
  }

  /**
   * @author yixi
   */
  public static class AntPath extends Value {

    public AntPath(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.antPath);
    }
  }

  /**
   * @author yixi
   */
  public static class Exists extends Value {

    public Exists(Object value, Boolean ignoreNull) {
      super(value, ignoreNull);
      this.setOperator(Operator.exists);
    }
  }

  /**
   * @author yixi
   */
  public static class Logic extends Pattern {

    private final List<Combine> combines = new ArrayList<>();

    public Logic add(Combine combine) {
      combines.add(combine);
      return this;
    }

    public List<Combine> combines() {
      return combines;
    }
  }

  /**
   * @author yixi
   */
  public static class Not extends Logic {

    public Not() {
      this.setOperator(Operator.__not);
    }
  }

  /**
   * @author yixi
   */
  public static class And extends Logic {

    public And() {
      this.setOperator(Operator.__and);
    }
  }

  /**
   * @author yixi
   */
  public static class Or extends Logic {

    public Or() {
      this.setOperator(Operator.__or);
    }
  }

  /**
   * @author yixi
   */
  public static class ArrayValue extends Pattern {

    private final List<Value> values = new ArrayList<>();

    public ArrayValue add(Value value) {
      values.add(value);
      return this;
    }

    public List<Value> values() {
      return values;
    }
  }

  /**
   * @author yixi
   */
  public static class Combine extends Pattern {

    private final Map<String, Pattern> combines = new LinkedHashMap<>();

    public Combine add(String key, Pattern pattern) {
      combines.put(key, pattern);
      return this;
    }

    public Map<String, Pattern> combines() {
      return combines;
    }

    @Override
    public Operator operator() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * @author yixi
   */
  public static class Custom extends Value {

    private final String type;

    public Custom(String type, Value value) {
      super(value.value(), value.ignoreNull());
      this.type = type;
      setOperator(value.operator());
    }

    public String getType() {
      return type;
    }
  }
}
