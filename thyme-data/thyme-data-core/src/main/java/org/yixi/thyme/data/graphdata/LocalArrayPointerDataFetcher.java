package org.yixi.thyme.data.graphdata;

import java.util.List;
import org.yixi.data.client.Fetch;
import org.yixi.thyme.data.BaseDao;

/**
 * @author yixi
 * @since 1.0.0
 */
public class LocalArrayPointerDataFetcher implements ArrayPointerDataFetcher, LocalDataFetcher {

  private final LocalDataFetcherDelegator localDataFetcher;

  public LocalArrayPointerDataFetcher(BaseDao baseDao) {
    this(Options.Builder.builder().baseManager(baseDao).build());
  }

  public LocalArrayPointerDataFetcher(Options options) {
    localDataFetcher = new LocalDataFetcherDelegator(options);
  }

  @Override
  public List<Object> fetch(Fetch fetch) {
    return localDataFetcher.fetch(fetch);
  }

  @Override
  public Options getOptions() {
    return localDataFetcher.getOptions();
  }
}
