package org.yixi.thyme.data.rest;

import java.util.List;
import org.yixi.data.client.BulkRequest;
import org.yixi.thyme.core.BaseEntity;

/**
 * @author yixi
 * @since 1.0.0
 */
public class BatchOperatorBuilder<T extends BaseEntity<String>> {

  private BulkRequest<T> bulkRequest = new BulkRequest<>();

  public static BatchOperatorBuilder builder() {
    return new BatchOperatorBuilder();
  }

  public BatchOperatorBuilder<T> creates(List<T> creates) {
    bulkRequest.setCreates(creates);
    return this;
  }

  public BatchOperatorBuilder<T> updates(List<T> updates) {
    bulkRequest.setCreates(updates);
    return this;
  }

  public BatchOperatorBuilder<T> replaces(List<T> replaces) {
    bulkRequest.setCreates(replaces);
    return this;
  }

  public BatchOperatorBuilder<T> deletes(List<T> deletes) {
    bulkRequest.setCreates(deletes);
    return this;
  }

  public BulkRequest<T> build() {
    return bulkRequest;
  }
}
