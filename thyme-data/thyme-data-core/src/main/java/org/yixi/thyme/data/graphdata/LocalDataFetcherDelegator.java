package org.yixi.thyme.data.graphdata;

import java.util.List;
import org.yixi.data.client.Fetch;
import org.yixi.thyme.core.CustomClassLoader;
import org.yixi.thyme.core.ex.FetchException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.BaseQuery;
import org.yixi.thyme.data.type.TypeConverters;

/**
 * @author yixi
 * @since 1.0.0
 */
public class LocalDataFetcherDelegator {

  private final LocalDataFetcher.Options options;
  private final CustomClassLoader customClassLoader = new CustomClassLoader();

  private final TypeConverters typeConverters;

  LocalDataFetcherDelegator(LocalDataFetcher.Options options) {
    this.options = options;
    this.typeConverters = new TypeConverters(options.getDataSourceType());
  }

  public List<Object> fetch(Fetch fetch) {
    try {
      List<Object> objects = options.getBaseManager().findMany(baseQuery(fetch));
      if (fetch.getFetchs() != null && objects.size() > 0) {
        options.getBaseManager().graphData(objects, fetch.getFetchs());
      }
      return objects;
    } catch (ThymeException e) {
      throw new FetchException(e.getMessage());
    }
  }

  public LocalDataFetcher.Options getOptions() {
    return options;
  }

  private BaseQuery baseQuery(Fetch fetch) {
    typeConverters.convert(fetch.getFilter());
    BaseQuery query = null;
    String className;
    if (options.getDataSourceType() == LocalDataFetcher.DataSourceType.Mysql) {
      className = "org.yixi.thyme.mysql.MysqlQuery";
    } else if (options.getDataSourceType() == LocalDataFetcher.DataSourceType.Mongo) {
      className = "org.yixi.thyme.mongo.MongoQuery";
    } else {
      throw new ThymeException("数据源未知");
    }
    Class clazz = customClassLoader.loadClass(className);
    if (clazz == null) {
      throw new ThymeException("ClassNotFound: " + className);
    }
    try {
      query = (BaseQuery) clazz.newInstance();
    } catch (Exception e) {
      // ignore
    }
    query.filter().addFilters(fetch.getFilter());
    query.skip(fetch.getSkip());
    query.limit(fetch.getLimit());
    query.fields(fetch.getFields());
    return query;
  }
}
