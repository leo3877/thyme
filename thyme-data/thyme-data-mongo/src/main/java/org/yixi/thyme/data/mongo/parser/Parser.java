package org.yixi.thyme.data.mongo.parser;

import org.yixi.data.client.YixiQuery;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface Parser<T> {

  T parse(YixiQuery query);
}
