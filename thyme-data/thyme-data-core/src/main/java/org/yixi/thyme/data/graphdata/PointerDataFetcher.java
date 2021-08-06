package org.yixi.thyme.data.graphdata;

import java.util.List;
import org.yixi.data.client.Fetch;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface PointerDataFetcher {

  List<Object> fetch(Fetch fetch);
}
