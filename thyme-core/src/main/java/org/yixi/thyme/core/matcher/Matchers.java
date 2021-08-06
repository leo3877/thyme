package org.yixi.thyme.core.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.Assertions;

/**
 * @author yixi
 * @since 1.2.0
 */
public abstract class Matchers {

  private static final StringMatcher stringMatcher = new StringMatcher();
  private static final NumberMatcher numberMatcher = new NumberMatcher();
  private static final BooleanMatcher booleanMatcher = new BooleanMatcher();
  private static final ArrayMatcher arrayMatcher = new ArrayMatcher();

  private static final ValueMatcher valueMatcher = new ValueMatcher();
  private static final CombineMatcher combineMatcher = new CombineMatcher();
  private static final LogicMatcher logicMatcher = new LogicMatcher();
  private static final CustomMatcher customMatcher = new CustomMatcher();

  static {
    customMatcher.registerHandler("Date", new DateMatcher());
  }

  public static void registerCustomTypeMatcher(String type, Matcher customTypeMatcher) {
    customMatcher.registerHandler(type, customTypeMatcher);
  }

  public static boolean match(Pattern pattern, Object target) {
    Assertions.notNull("pattern", pattern);
    Assertions.notNull("target", target);
    if (pattern instanceof Pattern.Logic) {
      return logicMatcher.match((Pattern.Logic) pattern, target);
    } else if (pattern instanceof Pattern.Combine) {
      return combineMatcher.match((Pattern.Combine) pattern, target);
    } else if (pattern instanceof Pattern.Value) {
      return valueMatcher.match((Pattern.Value) pattern, target);
    } else {
      throw new UnsupportedOperationException(
        "Unsupported Pattern: " + pattern.getClass().getSimpleName());
    }
  }

  /**
   * @author yixi
   */
  public interface Matcher<P extends Pattern, V> {

    boolean match(P pattern, V target);

    default String type(Object target) {
      return target instanceof JsonNode ? ((JsonNode) target).getNodeType().name()
        : target.getClass().getSimpleName();
    }

    default String string(Object target) {
      if (target instanceof String) {
        return (String) target;
      } else if (target instanceof JsonNode && ((JsonNode) target).isTextual()) {
        return ((JsonNode) target).asText();
      } else {
        throw new IllegalArgumentException(
          "error type：" + type(target) + ", Must be String, value: " + target);
      }
    }

    default double number(Object target) {
      if (target instanceof Number) {
        return ((Number) target).doubleValue();
      } else if (target instanceof JsonNode && ((JsonNode) target).isNumber()) {
        return ((JsonNode) target).asDouble();
      } else {
        throw new IllegalArgumentException(
          "error type：" + type(target) + ", Must be Number, value: " + target);
      }
    }

    default boolean bool(Object target) {
      if (target instanceof Boolean) {
        return (Boolean) target;
      } else if (target instanceof JsonNode && ((JsonNode) target).isBoolean()) {
        return ((JsonNode) target).asBoolean();
      } else {
        throw new IllegalArgumentException(
          "error type：" + type(target) + ", Must be Boolean, value: " + target);
      }
    }
  }

  /**
   * @author yixi
   */
  public static class StringMatcher implements Matcher<Pattern.Value, String> {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean match(Pattern.Value pattern, String target) {
      if (pattern.value() instanceof String) {
        String value = (String) pattern.value();
        if (pattern.operator() == Operator.eq) {
          return value.equals(target);
        } else if (pattern.operator() == Operator.ne) {
          return !value.equals(target);
        } else if (pattern.operator() == Operator.lt) {
          return target.compareTo(value) < 0;
        } else if (pattern.operator() == Operator.lte) {
          return target.compareTo(value) <= 0;
        } else if (pattern.operator() == Operator.gt) {
          return target.compareTo(value) > 0;
        } else if (pattern.operator() == Operator.gte) {
          return target.compareTo(value) >= 0;
        } else if (pattern.operator() == Operator.contains) {
          return target.contains(value);
        } else if (pattern.operator() == Operator.startsWith) {
          return target.startsWith(value);
        } else if (pattern.operator() == Operator.endsWith) {
          return target.endsWith(value);
        } else if (pattern.operator() == Operator.regex) {
          return java.util.regex.Pattern.matches(string(pattern.value()), target);
        } else if (pattern.operator() == Operator.antPath) {
          return antPathMatcher.match(string(pattern.value()), target);
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in String type, operator " + pattern.operator());
        }
      } else if (pattern.value() instanceof List) {
        List<Object> l = (List) pattern.value();
        if (pattern.operator() == Operator.in) {
          return l.stream().anyMatch(obj -> string(obj).equals(target));
        } else if (pattern.operator() == Operator.nin) {
          return !l.stream().anyMatch(obj -> string(obj).equals(target));
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in List type, operator " + pattern.operator());
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid data Type, must be String. type:  " + pattern.value()
            .getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class DateMatcher implements Matcher<Pattern.Value, Object> {

    @Override
    public boolean match(Pattern.Value pattern, Object obj) {
      Date target = parse(obj);
      if (pattern.value() instanceof String) {
        Date source = parse(pattern.value());
        if (pattern.operator() == Operator.eq) {
          return target.compareTo(source) == 0;
        } else if (pattern.operator() == Operator.ne) {
          return target.compareTo(source) != 0;
        } else if (pattern.operator() == Operator.lt) {
          return target.compareTo(source) < 0;
        } else if (pattern.operator() == Operator.lte) {
          return target.compareTo(source) <= 0;
        } else if (pattern.operator() == Operator.gt) {
          return target.compareTo(source) > 0;
        } else if (pattern.operator() == Operator.gte) {
          return target.compareTo(source) >= 0;
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in String type, operator " + pattern.operator());
        }
      } else if (pattern.value() instanceof List) {
        List<Object> l = (List) pattern.value();
        List<Date> dates = new ArrayList<>();
        l.forEach(v -> dates.add(parse(v)));
        if (pattern.operator() == Operator.in) {
          return dates.stream().anyMatch(o -> o.compareTo(target) == 0);
        } else if (pattern.operator() == Operator.nin) {
          return dates.stream().anyMatch(o -> o.compareTo(target) != 0);
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in List type, operator " + pattern.operator());
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid data Type, must be String. type:  " + pattern.value()
            .getClass().getSimpleName());
      }
    }

    public Date parse(Object date) {
      if (date instanceof Date) {
        return (Date) date;
      } else if (date instanceof String || date instanceof TextNode) {
        try {
          return Thyme.DATE_TIME_FORMAT_GMT_8.parse(
            date instanceof String ? (String) date : ((TextNode) date).asText());
        } catch (ParseException e) {
          throw new IllegalArgumentException("Invalid Date. date: "
            + date
            + "format should be: "
            + Thyme.DATE_TIME_FORMAT);
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid date type, must be String. type: " + date.getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class NumberMatcher implements Matcher<Pattern.Value, Double> {

    @Override
    public boolean match(Pattern.Value pattern, Double target) {
      if (pattern.value() instanceof Number) {
        double num = number(pattern.value());
        if (pattern.operator() == Operator.eq) {
          return num == target;
        } else if (pattern.operator() == Operator.ne) {
          return num != target;
        } else if (pattern.operator() == Operator.lt) {
          return target < num;
        } else if (pattern.operator() == Operator.lte) {
          return target <= num;
        } else if (pattern.operator() == Operator.gt) {
          return target > num;
        } else if (pattern.operator() == Operator.gte) {
          return target >= num;
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in Number type, operator " + pattern.operator());
        }
      } else if (pattern.value() instanceof List) {
        List<Object> l = (List) pattern.value();
        if (pattern.operator() == Operator.in) {
          return l.stream().anyMatch(obj -> number(obj) == target);
        } else if (pattern.operator() == Operator.nin) {
          return !l.stream().anyMatch(obj -> number(obj) == target);
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in List type, operator " + pattern.operator());
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid data Type, must be Number. type:  " + pattern.value()
            .getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class BooleanMatcher implements Matcher<Pattern.Value, Boolean> {

    @Override
    public boolean match(Pattern.Value pattern, Boolean target) {
      if (pattern.value() instanceof Boolean) {
        boolean bool = bool(pattern.value());
        if (pattern.operator() == Operator.eq) {
          return bool == target;
        } else if (pattern.operator() == Operator.ne) {
          return bool != target;
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in Boolean type, operator " + pattern.operator());
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid data Type, must be Boolean. type:  " + pattern.value()
            .getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class ArrayMatcher implements Matcher<Pattern.Value, List> {

    @Override
    public boolean match(Pattern.Value pattern, List target) {
      if (pattern.value() instanceof List) {
        List<Object> l = (List) pattern.value();
        Predicate predicate = obj -> target.stream().anyMatch(t -> {
          if (t instanceof String) {
            return string(obj).equals(t);
          } else if (t instanceof Number) {
            return number(obj) == number(t);
          } else {
            throw new IllegalArgumentException("Unsupported Date Type: " + target);
          }
        });
        if (pattern.operator() == Operator.anyOf) {
          return l.stream().anyMatch(predicate);
        } else if (pattern.operator() == Operator.allOf) {
          return l.stream().allMatch(predicate);
        } else if (pattern.operator() == Operator.notOf) {
          return !l.stream().anyMatch(predicate);
        } else {
          throw new IllegalArgumentException(
            "Invalid operator in Array type, operator " + pattern.operator());
        }
      } else {
        throw new IllegalArgumentException(
          "Invalid data Type, must be Array. type:  " + pattern.value()
            .getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class ValueMatcher implements Matcher<Pattern.Value, Object> {

    @Override
    public boolean match(Pattern.Value value, Object target) {
      if (target == null && value.ignoreNull()) {
        return true;
      } else if (value.operator() == Operator.exists) {
        return target != null;
      } else if (target == null) {
        return false;
      } else if (value instanceof Pattern.Custom) {
        return customMatcher.match((Pattern.Custom) value, target);
      } else if (target instanceof String || target instanceof TextNode) {
        return stringMatcher.match(value,
          target instanceof String ? (String) target : ((TextNode) target).asText());
      } else if (target instanceof Number || target instanceof NumericNode) {
        return numberMatcher.match(value, number(target));
      } else if (target instanceof Boolean || target instanceof BooleanNode) {
        return booleanMatcher.match(value, bool(target));
      } else if (target instanceof List || target instanceof ArrayNode) {
        List l;
        if (target instanceof List) {
          l = (List) target;
        } else {
          l = new ArrayList();
          ((ArrayNode) target).elements().forEachRemaining(l::add);
        }
        return arrayMatcher.match(value, l);
      } else {
        throw new UnsupportedOperationException(
          "Unsupported data Type: " + target.getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class LogicMatcher implements Matcher<Pattern.Logic, Object> {

    @Override
    public boolean match(Pattern.Logic logic, Object target) {
      if (logic instanceof Pattern.And) {
        return logic.combines()
          .stream()
          .allMatch(combine -> combineMatcher.match(combine, target));
      } else if (logic instanceof Pattern.Or) {
        return logic.combines()
          .stream()
          .anyMatch(combine -> combineMatcher.match(combine, target));
      } else if (logic instanceof Pattern.Not) {
        if (logic.combines().size() == 1) {
          return !combineMatcher.match(logic.combines().get(0), target);
        } else {
          throw new IllegalArgumentException("Error Not operator: " + logic.combines());
        }
      } else {
        throw new IllegalArgumentException(
          "Unsupported Pattern:  " + logic.getClass().getSimpleName());
      }
    }
  }

  /**
   * @author yixi
   */
  public static class CustomMatcher implements Matcher<Pattern.Custom, Object> {

    private final Map<String, Matcher> handlers = new ConcurrentHashMap<>();

    @Override
    public boolean match(Pattern.Custom custom, Object target) {
      Matcher matcher = handlers.get(custom.getType());
      if (matcher != null) {
        return matcher.match(custom, target);
      } else {
        throw new IllegalArgumentException("unknown CustomTypeHandler, type: " + custom.getType());
      }
    }

    public void registerHandler(String type, Matcher matcher) {
      handlers.put(type, matcher);
    }
  }

  /**
   * @author yixi
   */
  public static class CombineMatcher implements Matcher<Pattern.Combine, Object> {

    private Object val(String filed, JsonNode target) {
      if (filed.indexOf(".") > 0) {
        String[] split = filed.split("\\.");
        JsonNode tmp = target;
        for (String s : split) {
          tmp = tmp.get(s);
          if (tmp == null) {
            return null;
          }
        }
        return tmp;
      } else {
        return target.get(filed);
      }
    }

    @Override
    public boolean match(Pattern.Combine combine, Object target) {
      Object obj = target instanceof DataSource ? target : Jsons.toJsonNode(target);
      return combine.combines()
        .entrySet().stream().allMatch(entry -> {
          String field = (String) ((Map.Entry) entry).getKey();
          Pattern p = (Pattern) ((Map.Entry) entry).getValue();
          if (p instanceof Pattern.Value) {
            return obj instanceof DataSource ? valueMatcher.match((Pattern.Value) p,
              ((DataSource) obj).call(field))
              : valueMatcher.match((Pattern.Value) p,
                val(field, (JsonNode) obj));
          } else if (p instanceof Pattern.ArrayValue) {
            for (Pattern.Value value : ((Pattern.ArrayValue) p).values()) {
              boolean res;
              if (obj instanceof DataSource) {
                res = valueMatcher.match(value, ((DataSource) obj).call(field));
              } else {
                res = valueMatcher.match(value, val(field, (JsonNode) obj));
              }
              if (!res) {
                return false;
              }
            }
            return true;
          } else if (p instanceof Pattern.Combine) {
            return this.match((Pattern.Combine) p, obj);
          } else if (p instanceof Pattern.Logic) {
            return logicMatcher.match((Pattern.Logic) p, obj);
          } else {
            throw new UnsupportedOperationException(
              "Unsupported Pattern: " + p.getClass().getSimpleName());
          }
        });
    }
  }
}
