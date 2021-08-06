package org.yixi.data.client;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface Queryable<T> {

  QueryResponse<T> query(YixiQuery query);
}
