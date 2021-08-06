package org.yixi.data.client;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.PoJoUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
@Data
public class YixiQuery implements Serializable {

  /**
   * 查询返回结果数限制
   */
  private int limit;
  /**
   * 跳过满足查询条件的结果集
   */
  private int skip;
  /**
   * 加载子资源或者子对象
   */
  private Map<String, Fetch> fechs;
  /**
   * 是否返回满足查询条件的结果集总数
   */
  private boolean count;
  /**
   * 排序
   */
  private Map<String, Integer> sorts = new LinkedHashMap<>();
  /**
   * 投影
   */
  private Map<String, Boolean> fields = new LinkedHashMap<>();
  /**
   * 查询条件
   */
  private Map<String, Object> filter = new LinkedHashMap<>();

  /**
   * 验证 filter 指令是否合法
   */
  public void validate() {
    Validator.validate(getFilter());
  }

  public void forEachFilter(Fn fn) {
    forEachField(filter, fn);
  }

  public YixiQuery stringToLong(Class clazz, boolean lowerUnderscore) {
    Map<String, Method> getterMethods = PoJoUtils.findGetterMethods(clazz, lowerUnderscore);
    if (lowerUnderscore) {
      lowerUnderscore();
    }
    forEachFilter((container, key, value) -> {
      if (value instanceof String
        && getterMethods.get(key) != null
        && getterMethods.get(key).getReturnType() == Long.class) {
        container.put(key, Long.valueOf((String) value));
      }
    });
    return this;
  }

  public YixiQuery lowerUnderscore() {
    forEachFilter((container, key, value) -> {
      String newKey = PoJoUtils.lowerUnderscore(key);
      if (!newKey.equals(key)) {
        container.put(newKey, value);
        container.remove(key);
      }
    });
    return this;
  }

  public static YixiQuery create(String json) {
    return Jsons.decode(json, YixiQuery.class);
  }

  public static YixiQuery create(String key, Object value) {
    YixiQuery yixiQuery = new YixiQuery();
    yixiQuery.setFilter(new Document<>(key, value));
    return yixiQuery;
  }

  public static YixiQuery create(Map<String, Object> filter) {
    return create(filter, 0);
  }

  public static YixiQuery create(Map<String, Object> filter, int limit) {
    YixiQuery yixiQuery = new YixiQuery();
    yixiQuery.setFilter(new Document<>(filter));
    yixiQuery.setLimit(limit);
    return yixiQuery;
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  private void forEachField(Map<String, Object> filter, Fn fn) {
    for (String key : new HashSet<>(filter.keySet())) {
      if ("__and".equals(key) || "__or".equals(key)) {
        Object o = filter.get(key);
        if (!(o instanceof List)) {
          throw Thyme.ex("syntax error, %s value must be array", key);
        }
        forEachField((List) o, fn);
      } else {
        fn.accept(filter, key, filter.get(key));
      }
    }
  }

  private void forEachField(List objects, Fn fn) {
    for (Object obj : objects) {
      if (!(obj instanceof Map)) {
        throw Thyme.ex("syntax error, %s must be Object", obj);
      }
      forEachField((Map) obj, fn);
    }
  }

  /**
   * @author yixi
   */
  public interface Fn {

    void accept(Map container, String key, Object value);
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public enum OrderType {

    DESC(-1),

    ASC(1);

    private final int code;

    OrderType(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Builder {

    private RestFilter filter;

    private int limit;
    private int skip;
    private boolean count;

    private Map<String, Integer> sorts;
    private Map<String, Boolean> fields;

    private Map<String, Fetch> fechs;

    public Builder() {
      this.filter = RestFilter.create();
    }

    public Builder(Map<String, Object> filter) {
      this.filter = RestFilter.create(filter);
    }

    public Builder(RestFilter filter) {
      this.filter = filter;
    }

    public Builder(YixiQuery yixiQuery) {
      filter = RestFilter.create(yixiQuery.getFilter());
      limit = yixiQuery.getLimit();
      skip = yixiQuery.getSkip();
      count = yixiQuery.isCount();
      sorts = new LinkedHashMap<>(yixiQuery.getSorts());
      fields = new LinkedHashMap<>(yixiQuery.getFields());
      fechs = yixiQuery.getFechs();
    }

    public static Builder builder() {
      return new Builder();
    }

    public static Builder builder(Map<String, Object> filter) {
      return new Builder(filter);
    }

    public static Builder builder(RestFilter filter) {
      return new Builder(filter);
    }

    public Builder limit(int limit) {
      this.limit = limit;
      return this;
    }

    public int limit() {
      return limit;
    }

    public Builder skip(int skip) {
      this.skip = skip;
      return this;
    }

    public int skip() {
      return skip;
    }

    public Builder count(boolean count) {
      this.count = count;
      return this;
    }

    public boolean isCount() {
      return count;
    }

    public Map<String, Integer> sorts() {
      return sorts;
    }

    public Builder sort(String key, OrderType orderType) {
      if (sorts == null) {
        sorts = new LinkedHashMap<>();
      }
      sorts.put(key, orderType.getCode());

      return this;
    }

    public Builder sort(Map<String, Integer> sorts) {
      if (this.sorts == null) {
        this.sorts = new LinkedHashMap<>();
      }
      this.sorts.putAll(sorts);

      return this;
    }

    public Builder field(String key, boolean show) {
      if (fields == null) {
        fields = new LinkedHashMap<>();
      }
      fields.put(key, show);
      return this;
    }

    public Builder fields(Map<String, Boolean> keys) {
      if (keys != null && !keys.isEmpty()) {
        if (fields == null) {
          fields = new LinkedHashMap<>();
        }
        fields.putAll(keys);
      }
      return this;
    }

    public Map<String, Boolean> fields() {
      return fields;
    }

    /**
     * Compound query chains to be more readable. Q.instance().key("age").$gt(18).$lt(45).key("str").$eq(3)
     */
    public Builder key(final String key) {
      filter.key(key);
      return this;
    }

    public Builder gt(final Object object) {
      filter.gt(object);
      return this;
    }

    public Builder gte(final Object object) {
      filter.gte(object);
      return this;
    }

    public Builder lt(final Object object) {
      filter.lt(object);
      return this;
    }

    public Builder lte(final Object object) {
      filter.lte(object);
      return this;
    }

    /**
     * Equivalent of the find({key:value})
     */
    public Builder eq(final Object object) {
      filter.eq(object);
      return this;
    }

    public Builder ne(final Object object) {
      filter.ne(object);
      return this;
    }

    public Builder in(final List<Object> objects) {
      filter.in(objects);
      return this;
    }

    public Builder notIn(final List<Object> objects) {
      filter.notIn(objects);
      return this;
    }

    public Builder exists(final Object object) {
      filter.exists(object);
      return this;
    }

    public Builder regex(final String regex) {
      filter.regex(regex);
      return this;
    }

    public Builder or(final RestFilter... filters) {
      filter.or(filters);
      return this;
    }

    public Builder and(final RestFilter... filters) {
      filter.and(filters);
      return this;
    }

    public Builder anyOf(final List items) {
      filter.anyOf(items);
      return this;
    }

    public Builder allOf(final List items) {
      filter.allOf(items);
      return this;
    }

    public Builder notOf(final List items) {
      filter.notOf(items);
      return this;
    }

    public Builder contains(final Object object) {
      filter.contains(object);
      return this;
    }

    public Builder startsWith(final Object object) {
      filter.startsWith(object);
      return this;
    }

    public Builder endsWith(final Object object) {
      filter.endsWith(object);
      return this;
    }

    public Builder nearSphere(final double longitude, final double latitude) {
      filter.nearSphere(longitude, latitude);
      return this;
    }

    public Builder nearSphere(final double longitude, final double latitude, int maxDistance) {
      filter.nearSphere(longitude, latitude, maxDistance);
      return this;
    }

    public Builder nearSphere(final double longitude, final double latitude, int minDistance,
      int maxDistance) {
      filter.nearSphere(longitude, latitude, minDistance, maxDistance);
      return this;
    }


    public Builder not() {
      filter.not();
      return this;
    }

    public Builder fetch(String schemaName, Fetch fetch) {
      return fetch(schemaName, null, fetch);
    }

    public Builder fetch(String schemaName, String alias, Fetch fetch) {
      if (fechs == null) {
        fechs = new LinkedHashMap<>();
      }
      fetch.setAlias(alias);
      fechs.put(schemaName, fetch);
      return this;
    }

    public Builder fetch(String schemaName, String key, String value) {
      return fech(schemaName, null, key, value);
    }

    public Builder fech(String schemaName, String alias, String key, String value) {
      if (fechs == null) {
        fechs = new LinkedHashMap<>();
      }
      Fetch fetch = new Fetch();
      fetch.setAlias(alias);
      fetch.getFilter().put(key, value);
      fechs.put(schemaName, fetch);
      return this;
    }

    public RestFilter getFilter() {
      return filter;
    }

    public YixiQuery build() {
      YixiQuery query = new YixiQuery();
      query.setCount(count);
      if (fields != null) {
        query.setFields(fields);
      }
      if (sorts != null) {
        query.setSorts(sorts);
      }
      query.setLimit(limit);
      query.setSkip(skip);
      query.setFilter(filter.filter());
      query.setFechs(fechs);
      return query;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Validator {

    public static final Map<String, Object> OPERATORS = new LinkedHashMap<>();

    static {
      OPERATORS.put("__and", 1);
      OPERATORS.put("__or", 1);
      OPERATORS.put("__not", 1);
      OPERATORS.put("eq", 1);
      OPERATORS.put("ne", 1);
      OPERATORS.put("gt", 1);
      OPERATORS.put("gte", 1);
      OPERATORS.put("lt", 1);
      OPERATORS.put("lte", 1);
      OPERATORS.put("exists", 1);
      OPERATORS.put("in", 1);
      OPERATORS.put("nin", 1);
      OPERATORS.put("regex", 1);
      OPERATORS.put("anyOf", 1);
      OPERATORS.put("allOf", 1);
      OPERATORS.put("notOf", 1);
      OPERATORS.put("contains", 1);
      OPERATORS.put("startsWith", 1);
      OPERATORS.put("endsWith", 1);
      OPERATORS.put("nearSphere", 1);
      OPERATORS.put("last", 1);
    }

    public static void validate(Map<String, Object> filter) {
      Set<String> keys = filter.keySet();
      for (String key : keys) {
        Object val = filter.get(key);
        if ("__and".equals(key) || "__or".equals(key)) {
          Object o = val;
          if (!(o instanceof List)) {
            throw Thyme.ex("syntax error, %s value must be array", key);
          }
          validate((List) o);
        } else if (val instanceof Map) {
          Object not = ((Map) val).get("__not");
          if (not != null) {
            if (not instanceof Map) {
              Set<String> notOperators = ((Map) val).keySet();
              for (String op : notOperators) {
                Object o = OPERATORS.get(op);
                if (o == null) {
                  throw Thyme.ex("syntax error. unknown operator: %s", key);
                }
              }
            }
          } else if (((Map) val).get("__date") != null) {
            continue;
          } else {
            Set<String> operators = ((Map) val).keySet();
            for (String op : operators) {
              Object o = OPERATORS.get(op);
              if (o == null) {
                throw Thyme.ex("syntax error. unknown operator: %s", key);
              } else if ("last".equals(op)) {
                Object last = ((Map) val).get(op);
                if (last instanceof String) {
                  String v = (String) last;
                  if (v.charAt(v.length() - 1) != 'd') {
                    throw Thyme.ex("last operator syntax error: %s", v);
                  }
                } else if (last instanceof Map) {
                  Map<String, Object> m = (Map) last;
                  for (String lastOp : m.keySet()) {
                    if (!lastOp.equals("eq")
                      && !lastOp.equals("gt")
                      && !lastOp.equals("gte")
                      && !lastOp.equals("lt")
                      && !lastOp.equals("lte")) {
                      throw Thyme.ex("last operator syntax error: %s", m);
                    }
                  }
                } else {
                  throw Thyme.ex("last operator syntax error, value type invalid: %s",
                    last.getClass().getSimpleName());
                }
              } else if ("nearSphere".equals(op)) {
                Object nearSphere = ((Map) val).get(op);
                if (nearSphere == null && !(nearSphere instanceof Map)) {
                  throw Thyme.ex("nearSphere syntax error.");
                }
                Map nearSphereMap = (Map) nearSphere;
                Object coordinates = nearSphereMap.get("coordinates");
                if (coordinates == null && !(nearSphere instanceof List)) {
                  throw Thyme.ex("nearSphere syntax error.");
                }
              }
            }
          }
        } else if (val instanceof List) {
          if (!("in".equals(key) || "nin".equals(key) || "anyOf".equals(key) || "allOf".equals(key)
            || "notOf".equals(key))) {
            throw Thyme.ex("syntax error, value must bu array. operator: %s value type: %s", key,
              val.getClass().getSimpleName());
          }
        }
      }
    }

    public static void validate(List<Object> objects) {
      for (Object obj : objects) {
        if (!(obj instanceof Map)) {
          throw Thyme.ex("syntax error, %s must be Object", obj);
        }
        validate((Map) obj);
      }
    }
  }
}
