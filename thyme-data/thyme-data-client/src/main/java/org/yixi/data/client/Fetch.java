package org.yixi.data.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author yixi
 * @since 1.0.0
 */
@Data
public class Fetch {

  private Options options = new Options();

  private String alias;
  private Type type;
  private Map<String, Object> filter = new LinkedHashMap<>();
  private Map<String, Boolean> fields = new LinkedHashMap<>();
  private int skip;
  private int limit;
  private Map<String, Fetch> fetchs;

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Options {

  }

  /**
   * @author yixi
   */
  public enum Type {

    Pointer,

    Relation
  }


  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Builder {

    private final RestFilter filter = RestFilter.create();
    private final Fetch fetch;

    public Builder() {
      this.fetch = new Fetch();
    }

    public static Builder builder() {
      return new Builder();
    }

    public Fetch build() {
      fetch.setFilter(filter.filter());
      return fetch;
    }

    public Builder limit(int limit) {
      fetch.setLimit(limit);
      return this;
    }

    public Builder skip(int skip) {
      fetch.setSkip(skip);
      return this;
    }

    public Builder field(String key, boolean show) {
      fetch.getFields().put(key, show);
      return this;
    }

    public Builder fields(Map<String, Boolean> keys) {
      fetch.getFields().putAll(keys);
      return this;
    }

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
  }
}