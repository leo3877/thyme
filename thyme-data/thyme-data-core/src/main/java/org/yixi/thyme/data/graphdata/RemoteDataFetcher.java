package org.yixi.thyme.data.graphdata;

import org.yixi.data.client.Fetch;
import org.yixi.data.client.Queryable;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * 数据加载器
 *
 * @author yixi
 * @since 1.0.0
 */
public interface RemoteDataFetcher extends DataFetcher {

  default YixiQuery toRestQuery(Fetch fetchers) {
    YixiQuery yixiQuery = new YixiQuery();
    yixiQuery.setFilter(fetchers.getFilter());
    yixiQuery.setSkip(fetchers.getSkip());
    yixiQuery.setLimit(fetchers.getLimit());
    yixiQuery.setFields(fetchers.getFields());
    yixiQuery.setFechs(fetchers.getFetchs());
    return yixiQuery;
  }

  Options getOptions();

  /**
   * @author yixi
   * @since 1.0.1
   */
  class Options {

    private Queryable queryable;
    private String field; // fetch 的具体字段

    public Queryable getQueryable() {
      return queryable;
    }

    public void setQueryable(Queryable queryable) {
      this.queryable = queryable;
    }

    public void setField(String field) {
      this.field = field;
    }

    public String getField() {
      return field;
    }

    /**
     * @author yixi
     * @since 1.0.0
     */
    public static class Builder {

      private Options options = new Options();

      public static Builder builder() {
        return new Builder();
      }

      public Builder field(String field) {
        options.setField(field);
        return this;
      }

      public Builder queryable(Queryable queryable) {
        options.setQueryable(queryable);
        return this;
      }

      public Options build() {
        if (options.getQueryable() == null) {
          throw new ThymeException("queryable must be specified.");
        }
        return options;
      }
    }
  }
}
