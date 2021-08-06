package org.yixi.thyme.data.graphdata;

import java.util.List;
import org.yixi.data.client.Fetch;
import org.yixi.data.client.Queryable;
import org.yixi.thyme.core.ex.FetchException;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * @author yixi
 * @since 1.0.0
 */
public class RemoteRelationDataFetcher<T> implements RelationDataFetcher, RemoteDataFetcher {

  private final Options options;

  public RemoteRelationDataFetcher(Queryable<T> queryable) {
    this(Options.Builder.builder().queryable(queryable).build());
  }

  public RemoteRelationDataFetcher(Options options) {
    this.options = options;
  }

  @Override
  public List<Object> fetch(Fetch fetch) {
    try {
      return options.getQueryable().query(toRestQuery(fetch)).getObjects();
    } catch (ThymeException e) {
      throw new FetchException(e.getMessage());
    }
  }

  @Override
  public Options getOptions() {
    return new Options();
  }
}
