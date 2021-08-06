package org.yixi.thyme.data.graphdata;

import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.BaseDao;

/**
 * 数据加载器
 *
 * @author yixi
 * @since 1.0.0
 */
public interface LocalDataFetcher extends DataFetcher {

  Options getOptions();

  /**
   * @author yixi
   * @since 1.0.1
   */
  class Options {

    private DataSourceType dataSourceType;
    private BaseDao baseDao;
    private String field; // fetch 的具体字段

    public DataSourceType getDataSourceType() {
      return dataSourceType;
    }

    public void setDataSourceType(DataSourceType dataSourceType) {
      this.dataSourceType = dataSourceType;
    }

    public BaseDao getBaseManager() {
      return baseDao;
    }

    public void setBaseManager(BaseDao baseDao) {
      this.baseDao = baseDao;
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

      public Builder baseManager(BaseDao baseDao) {
        options.setBaseManager(baseDao);
        return this;
      }

      public Options build() {
        if (options.getBaseManager() == null) {
          throw new ThymeException("baseManager must be specified.");
        }

        boolean mongoBaseManager = findInterface(options.getBaseManager().getClass(),
          "MongoBaseManager");
        if (mongoBaseManager) {
          options.setDataSourceType(DataSourceType.Mongo);
        } else {
          options.setDataSourceType(DataSourceType.Mysql);
        }

        return options;
      }

      private boolean findInterface(Class clazz, String className) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class parent : interfaces) {
          if (className.equals(parent.getSimpleName())) {
            return true;
          } else {
            boolean res = findInterface(parent, className);
            if (res) {
              return true;
            }
          }
        }
        return false;
      }
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  enum DataSourceType {

    Mysql,

    Mongo
  }
}
