package org.yixi.thyme.data.rest;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.ReflectionUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
public class DefaultQueryValidation implements org.yixi.thyme.data.rest.QueryValidation {

  private static final List<QueryValidation> validations = new CopyOnWriteArrayList<>();
  private final Class domainClass;

  public DefaultQueryValidation(Class domainClass) {
    validations.add(new QueryOperatorValidation());
    validations.add(new QueryParamValidation(domainClass));
    this.domainClass = domainClass;
  }

  public static void main(String[] args) {
    org.yixi.thyme.data.rest.QueryValidation queryValidation = new DefaultQueryValidation(
      TestObject.class);
    queryValidation.validation(
      new YixiQuery.Builder()
        .key("strs")
        .gt("rest")
        .key("a")
        .eq("rest")
        .key("testObject3.string3")
        .eq("hello")
        .key("updatedTime")
        .eq("dfafa")
        .build());
  }

  public void registerQueryValidation(org.yixi.thyme.data.rest.QueryValidation queryValidation) {
    validations.add(queryValidation);
  }

  @Override
  public void validation(YixiQuery yixiQuery) {
    for (org.yixi.thyme.data.rest.QueryValidation validation : validations) {
      validation.validation(yixiQuery);
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryOperatorValidation implements org.yixi.thyme.data.rest.QueryValidation {

    public static final Map<String, Object> MODIFIERS = new LinkedHashMap<>();

    static {
      MODIFIERS.put("$eq", 1);
      MODIFIERS.put("$ne", 1);
      MODIFIERS.put("$gt", 1);
      MODIFIERS.put("$gte", 1);
      MODIFIERS.put("$lt", 1);
      MODIFIERS.put("$lte", 1);
      MODIFIERS.put("$exists", 1);
      MODIFIERS.put("$in", 1);
      MODIFIERS.put("$nin", 1);
      MODIFIERS.put("$and", 1);
      MODIFIERS.put("$or", 1);
      MODIFIERS.put("$regex", 1);
    }

    @Override
    public void validation(YixiQuery yixiQuery) {
      yixiQuery.forEachFilter((container, field, operators) -> {
        if (operators instanceof Map) {
          Set<String> modifierSet = ((Map) operators).keySet();
          for (String modifierKey : modifierSet) {
            Object o = MODIFIERS.get(modifierKey);
            if (o == null) {
              throw new ThymeException("syntax error. unknown operator: " + field);
            }
          }
        }
      });
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryParamValidation implements org.yixi.thyme.data.rest.QueryValidation {

    private QueryKeys queryKeys;

    public QueryParamValidation(Class clazz) {
      queryKeys = new QueryKeys(QueryKeyResolver.resolve(clazz));
    }

    @Override
    public void validation(YixiQuery query) {
      Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap = queryKeys.getQueryKeyMap();
      query.forEachFilter((fcontainer, field, operators) -> {
        if (queryKeyMap.get(field) == null) {
          throw new ThymeException("Query Error. Unsupported query field: " + field +
            ", support fields: " + queryKeyMap.keySet());
        } else if (queryKeyMap.get(field).operators().length > 0) {
          validationOperator(operators, queryKeyMap, field);
        }
      });
      validationCondition(query.getFilter());
    }

    private void validationCondition(Map filter) {
      if (!queryKeys.getConditionAll().isEmpty()) {
        Set<Map.Entry<String, org.yixi.thyme.data.rest.QueryKey>> entries = queryKeys
          .getConditionAll().entrySet();
        for (Map.Entry<String, org.yixi.thyme.data.rest.QueryKey> entry : entries) {
          if (filter.get(entry.getKey()) == null
            && !exists(filter.get("$and"), entry.getKey())
            && !exists(filter.get("$or"), entry.getKey())) {
            throw new ThymeException(
              "Query required fields: " + queryKeys.getConditionAll().keySet());
          }
        }
      }

      // TODO validation Any
    }

    private void validationOperator(Object val,
      Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap, String key) {
      if (val instanceof Map) {
        Set<String> operators = ((Map) val).keySet();
        for (String operator : operators) {
          org.yixi.thyme.data.rest.QueryKey.Operator[] so = queryKeyMap.get(key).operators();
          boolean f = true;
          for (org.yixi.thyme.data.rest.QueryKey.Operator s : so) {
            if (operator.equals(s.name())) {
              f = false;
              break;
            }
          }
          if (f) {
            throw new ThymeException("Query Error. Unsupported query operator: "
              + operator
              + " on field "
              + key + ", support operators: " + operatorString(so));
          }
        }
      } else {
        org.yixi.thyme.data.rest.QueryKey.Operator[] so = queryKeyMap.get(key).operators();
        boolean f = true;
        for (org.yixi.thyme.data.rest.QueryKey.Operator s : so) {
          if ("$eq".equals(s.name())) {
            f = false;
            break;
          }
        }
        if (f) {
          throw new ThymeException(
            "Query Error. Unsupported query operator: $eq on field " + key);
        }
      }
    }

    private boolean exists(Object filters, String key) {
      if (filters != null && (filters instanceof List)) {
        for (Object o : (List) filters) {
          if (o instanceof Map && ((Map) o).get(key) != null) {
            return true;
          }
        }
      }
      return false;
    }

    private String operatorString(org.yixi.thyme.data.rest.QueryKey.Operator[] operators) {
      String s = "";
      for (org.yixi.thyme.data.rest.QueryKey.Operator operator : operators) {
        s += operator.name() + ", ";
      }
      return s.substring(0, s.length() - 2);
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryKeyResolver {

    public static Map<String, org.yixi.thyme.data.rest.QueryKey> resolve(Class clazz) {
      Map<String, org.yixi.thyme.data.rest.QueryKey> resolve = resolve(null, clazz);
      return resolve;
    }

    public static Map<String, org.yixi.thyme.data.rest.QueryKey> resolve(String prefix,
      Class clazz) {
      Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap = new HashMap<>();

      ReflectionUtils.doWithFields(clazz, f -> {
        org.yixi.thyme.data.rest.QueryKey queryKey = f.getAnnotation(
          org.yixi.thyme.data.rest.QueryKey.class);
        if (queryKey != null) {
          if (prefix == null) {
            queryKeyMap.put(f.getName(), queryKey);
          } else {
            queryKeyMap.put(prefix + f.getName(), queryKey);
          }
        } else if (continueResolve(f.getGenericType())) {
          resolveChild(queryKeyMap, f);
        }
      });
      return queryKeyMap;
    }

    private static void resolveChild(Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap,
      Field field) {
      if (field.getGenericType() instanceof ParameterizedType) {
        ParameterizedType t = (ParameterizedType) field.getGenericType();
        if (Map.class.isAssignableFrom((Class<?>) t.getRawType())) {
          Type[] at = t.getActualTypeArguments();
          if (at.length == 2) {
            queryKeyMap.putAll(resolve(field.getName() + ".", (Class) at[1]));
          }
        } else if (List.class.isAssignableFrom((Class<?>) t.getRawType())) {
          Type[] at = t.getActualTypeArguments();
          if (at.length == 1) {
            queryKeyMap.putAll(resolve(field.getName() + ".", (Class) at[0]));
          }
        }
      } else {
        queryKeyMap.putAll(
          resolve(field.getName() + ".", (Class) field.getGenericType()));
      }
    }

    private static boolean continueResolve(Object val) {
      return val != String.class
        && val != Integer.class
        && val != int.class
        && val != Float.class
        && val != float.class
        && val != Double.class
        && val != double.class
        && val != char.class
        && val != Character.class
        && val != Long.class
        && val != long.class
        && val != short.class
        && val != Short.class
        && val != Date.class
        && val != Boolean.class
        && val != boolean.class;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryKeys {

    private Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap;
    private Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAll = new HashMap<>();
    private Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny = new HashMap<>();
    private Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny2 = new HashMap<>();
    private Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny3 = new HashMap<>();

    public QueryKeys(Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap) {
      this.queryKeyMap = queryKeyMap;
      queryKeyMap.forEach((k, v) -> {
        if (v.require() == org.yixi.thyme.data.rest.QueryKey.Condition.ALL) {
          conditionAll.put(k, v);
        } else if (v.require() == org.yixi.thyme.data.rest.QueryKey.Condition.ANY) {
          conditionAny.put(k, v);
        } else if (v.require() == org.yixi.thyme.data.rest.QueryKey.Condition.ANY2) {
          conditionAny2.put(k, v);
        } else if (v.require() == org.yixi.thyme.data.rest.QueryKey.Condition.ANY3) {
          conditionAny3.put(k, v);
        }
      });
    }

    public Map<String, org.yixi.thyme.data.rest.QueryKey> getQueryKeyMap() {
      return queryKeyMap;
    }

    public void setQueryKeyMap(
      Map<String, org.yixi.thyme.data.rest.QueryKey> queryKeyMap) {
      this.queryKeyMap = queryKeyMap;
    }

    public Map<String, org.yixi.thyme.data.rest.QueryKey> getConditionAll() {
      return conditionAll;
    }

    public void setConditionAll(
      Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAll) {
      this.conditionAll = conditionAll;
    }

    public Map<String, org.yixi.thyme.data.rest.QueryKey> getConditionAny() {
      return conditionAny;
    }

    public void setConditionAny(
      Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny) {
      this.conditionAny = conditionAny;
    }

    public Map<String, org.yixi.thyme.data.rest.QueryKey> getConditionAny2() {
      return conditionAny2;
    }

    public void setConditionAny2(
      Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny2) {
      this.conditionAny2 = conditionAny2;
    }

    public Map<String, org.yixi.thyme.data.rest.QueryKey> getConditionAny3() {
      return conditionAny3;
    }

    public void setConditionAny3(
      Map<String, org.yixi.thyme.data.rest.QueryKey> conditionAny3) {
      this.conditionAny3 = conditionAny3;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class TestObject extends BaseEntity<String> {

    private String id;

    @org.yixi.thyme.data.rest.QueryKey(require = org.yixi.thyme.data.rest.QueryKey.Condition.ALL)
    private Integer a;

    @org.yixi.thyme.data.rest.QueryKey(require = org.yixi.thyme.data.rest.QueryKey.Condition.ALL, operators = {
      org.yixi.thyme.data.rest.QueryKey.Operator.$in, org.yixi.thyme.data.rest.QueryKey
      .Operator.$eq})
    private List<String> strs;

    private List<TestObject3> listChild;

    private Map<String, TestObject2> mapChild;

    private TestObject3 testObject3;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class TestObject2 {

    private String id;
    @org.yixi.thyme.data.rest.QueryKey
    private String string2;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class TestObject3 {

    private String id;
    @org.yixi.thyme.data.rest.QueryKey
    private String string3;
  }
}
