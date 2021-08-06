package org.yixi.thyme.data.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.yixi.thyme.core.Document;

/**
 * @author yixi
 * @since 1.0.0
 */
public class UpdateOperator {

  private Document<String, Object> filter = new Document<>();
  private Document<String, Map<String, Object>> operators = new Document<>();
  private Document<String, Object> options = new Document<>();

  public Document<String, Object> getFilter() {
    return filter;
  }

  public void setFilter(Document<String, Object> filter) {
    this.filter = filter;
  }

  public Map<String, Map<String, Object>> getOperators() {
    return operators;
  }

  public void setOperators(
    Document<String, Map<String, Object>> operators) {
    this.operators = operators;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions(
    Document<String, Object> options) {
    this.options = options;
  }

  public void validate() {
    Operators.validate(this.getOperators());
  }

  /**
   * @author sneaky
   * @since 1.0.0
   */
  public static class Operators {

    public static final Map<String, Object> MODIFIERS = new LinkedHashMap<>();

    static {
      MODIFIERS.put("$set", 1);
      MODIFIERS.put("$unset", 1);
      MODIFIERS.put("$inc", 1);
      MODIFIERS.put("$mul", 1);
      MODIFIERS.put("$min", 1);
      MODIFIERS.put("$max", 1);
      MODIFIERS.put("$addToSet", 1);
      MODIFIERS.put("$pop", 1);
      MODIFIERS.put("$pullAll", 1);
      MODIFIERS.put("$push", 1);
    }

    public static void validate(Map<String, Map<String, Object>> operators) {
      Set<String> keys = operators.keySet();
      for (String op : keys) {
        if (MODIFIERS.get(op) == null) {
          throw new IllegalArgumentException("syntax error. unknown operator: " + op);
        }
        Object val = operators.get(op);
        if (!(val instanceof Map)) {
          throw new IllegalArgumentException("syntax error." + val + " must be Object");
        }
      }
    }
  }
}
