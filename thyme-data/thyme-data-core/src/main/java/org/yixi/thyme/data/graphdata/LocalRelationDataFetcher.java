package org.yixi.thyme.data.graphdata;

import java.util.List;
import org.yixi.data.client.Fetch;
import org.yixi.thyme.data.BaseDao;

/**
 * @author yixi
 * @since 1.0.0
 */
public class LocalRelationDataFetcher implements RelationDataFetcher, LocalDataFetcher {

  private LocalDataFetcherDelegator localDataFetcherDelegator;

  public LocalRelationDataFetcher(BaseDao baseDao) {
    this(Options.Builder.builder().baseManager(baseDao).build());
  }

  public LocalRelationDataFetcher(Options options) {
    this.localDataFetcherDelegator = new LocalDataFetcherDelegator(options);
  }

  @Override
  public List<Object> fetch(Fetch fetch) {
    return localDataFetcherDelegator.fetch(fetch);
  }

  @Override
  public Options getOptions() {
    return localDataFetcherDelegator.getOptions();
  }
}
