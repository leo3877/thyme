package org.yixi.thyme.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.util.Assertions;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class BaseQuery<Query extends BaseQuery, F extends BaseQuery.Filter> {

  protected F filter;

  protected int limit;
  protected int skip;
  protected boolean count;

  protected Document<String, Integer> sorts;
  protected Document<String, Boolean> fields;

  public abstract Query copy();

  public BaseQuery(F filter) {
    Assertions.notNull0(filter, "filter must be specified");
    this.filter = (F) filter.copy();
  }

  public Query limit(int limit) {
    this.limit = limit;
    return (Query) this;
  }

  public int limit() {
    return limit;
  }

  public Query skip(int skip) {
    this.skip = skip;
    return (Query) this;
  }

  public int skip() {
    return skip;
  }

  public Query count(boolean count) {
    this.count = count;
    return (Query) this;
  }

  public boolean isCount() {
    return count;
  }

  public Document<String, Integer> sorts() {
    return sorts;
  }

  public Query sort(String key, OrderType orderType) {
    if (sorts == null) {
      sorts = new Document<>();
    }
    sorts.put(key, orderType.getCode());

    return (Query) this;
  }

  public Query sort(Map<String, Integer> sorts) {
    if (this.sorts == null) {
      this.sorts = new Document<>();
    }
    this.sorts.putAll(sorts);

    return (Query) this;
  }

  public Query field(String key, boolean show) {
    if (fields == null) {
      fields = new Document<>();
    }
    fields.put(key, show);
    return (Query) this;
  }

  public Query fields(Map<String, Boolean> keys) {
    if (keys != null && !keys.isEmpty()) {
      if (fields == null) {
        fields = new Document<>();
      }
      fields.putAll(keys);
    }
    return (Query) this;
  }

  public Document<String, Boolean> fields() {
    return fields;
  }

  /**
   * Compound query chains to be more readable. Q.instance().key("age").$gt(18).$lt(45).key("str").$eq(3)
   */
  public Query key(final String key) {
    filter.key(key);
    return (Query) this;
  }

  public Query gt(final Object object) {
    filter.gt(object);
    return (Query) this;
  }

  public Query gte(final Object object) {
    filter.gte(object);
    return (Query) this;
  }

  public Query lt(final Object object) {
    filter.lt(object);
    return (Query) this;
  }

  public Query lte(final Object object) {
    filter.lte(object);
    return (Query) this;
  }

  /**
   * Equivalent of the find({key:value})
   */
  public Query eq(final Object object) {
    filter.eq(object);
    return (Query) this;
  }

  public Query ne(final Object object) {
    filter.ne(object);
    return (Query) this;
  }

  public Query in(final List<Object> objects) {
    filter.in(objects);
    return (Query) this;
  }

  public Query notIn(final List<Object> objects) {
    filter.notIn(objects);
    return (Query) this;
  }

  public Query regex(final Pattern regex) {
    filter.regex(regex);
    return (Query) this;
  }

  public Query exists(final Boolean exists) {
    filter.exists(exists);
    return (Query) this;
  }

  /**
   * Equivalent to an $or operand
   */
  public Query or(F... filters) {
    filter.or(filters);
    return (Query) this;
  }

  /**
   * Equivalent to an $and operand
   */
  public Query and(final F... filters) {
    filter.and(filters);
    return (Query) this;
  }

  public Query filter(F f) {
    this.filter = f;
    return (Query) this;
  }

  public F filter() {
    return filter;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class QueryException extends RuntimeException {

    private static final long serialVersionUID = 380785032111523828L;

    public QueryException(final String message) {
      super(message);
    }
  }

  /**
   * @author yixi
   */
  public static class NullObject {

  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  public abstract static class Filter<F extends Filter> {

    protected Document<String, Object> filters = new Document<>();
    protected String currentKey;
    protected boolean hasNot;

    public Filter() {
      filters = new Document<>();
    }

    public Filter(Map<String, Object> filters) {
      Assertions.notNull("filter must be specified", filters);
      this.filters.putAll(filters);
    }

    public abstract F copy();

    public Document<String, Object> doc() {
      return filters;
    }

    public F addFilters(Map<String, Object> filters) {
      this.filters.putAll(filters);
      return (F) this;
    }

    /**
     * Compound query chains to be more readable. Q.instance().key("age").$gt(18).$lt(45).key("str").$eq(3)
     */
    public F key(final String key) {
      currentKey = key;
      if (filters.get(key) == null) {
        filters.put(currentKey, new NullObject());
      }
      return (F) this;
    }

    public F gt(final Object object) {
      addOperand(QueryOperators.GT, object);
      return (F) this;
    }

    public F gte(final Object object) {
      addOperand(QueryOperators.GTE, object);
      return (F) this;
    }

    public F lt(final Object object) {
      addOperand(QueryOperators.LT, object);
      return (F) this;
    }

    public F lte(final Object object) {
      addOperand(QueryOperators.LTE, object);
      return (F) this;
    }

    public F eq(final Object object) {
      addOperand(null, object);
      return (F) this;
    }

    public F ne(final Object object) {
      addOperand(QueryOperators.NE, object);
      return (F) this;
    }

    public F in(final List<Object> objects) {
      List l = (List) this.filters.get(QueryOperators.OR);
      if (l == null) {
        l = new ArrayList();
        this.filters.put(QueryOperators.OR, l);
      }
      for (Object object : objects) {
        l.add(new Document<>(currentKey, object));
      }
      filters.remove(currentKey);
      return (F) this;
    }

    public F notIn(final List<Object> objects) {
      addOperand(QueryOperators.NIN, objects);
      return (F) this;
    }

    public F regex(final Pattern regex) {
      addOperand(null, regex);
      return (F) this;
    }

    public F exists(final Boolean exists) {
      addOperand(QueryOperators.EXISTS, exists);
      return (F) this;
    }

    public F or(final F... filters) {
      List l = (List) this.filters.get(QueryOperators.OR);
      if (l == null) {
        l = new ArrayList();
        this.filters.put(QueryOperators.OR, l);
      }
      for (Filter filter : filters) {
        l.add(filter.doc());
      }
      return (F) this;
    }

    public F and(final F... filters) {
      List l = (List) this.filters.get(QueryOperators.AND);
      if (l == null) {
        l = new ArrayList();
        this.filters.put(QueryOperators.AND, l);
      }
      for (Filter filter : filters) {
        l.add(filter.doc());
      }
      return (F) this;
    }

    protected void addOperand(final String op, final Object value) {
      Object valueToPut = value;
      if (op == null) {
        if (hasNot) {
          valueToPut = new Document<>(QueryOperators.NOT, valueToPut);
          hasNot = false;
        }
        filters.put(currentKey, valueToPut);
        return;
      }

      Object storedValue = filters.get(currentKey);
      Document<String, Object> operand;
      if (!(storedValue instanceof Document)) {
        operand = new Document<>();
        if (hasNot) {
          Document<String, Object> notOperand = new Document<>(QueryOperators.NOT, operand);
          filters.put(currentKey, notOperand);
          hasNot = false;
        } else {
          filters.put(currentKey, operand);
        }
      } else {
        operand = (Document<String, Object>) filters.get(currentKey);
        if (operand.get(QueryOperators.NOT) != null) {
          operand = (Document<String, Object>) operand.get(QueryOperators.NOT);
        }
      }

      operand.put(op, valueToPut);
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public enum OrderType {

    DESC(-1),

    ASC(1);

    private int code;

    OrderType(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }
}
