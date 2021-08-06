package org.yixi.data.client;

import java.io.Serializable;

/**
 * 未找到就新增
 *
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class UpsertResponse<PK> implements Serializable {

  public static final String COUNT_KEY = "count";

  private Integer count;
  private PK id;

  public static <PK> UpsertResponse create(Integer count, PK id) {
    UpsertResponse<PK> upsertResponse = new UpsertResponse();
    upsertResponse.setCount(count);
    upsertResponse.setId(id);
    return upsertResponse;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public PK getId() {
    return id;
  }

  public void setId(PK id) {
    this.id = id;
  }
}
