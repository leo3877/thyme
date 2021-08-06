package org.yixi.data.client;

import java.io.Serializable;

/**
 * @author yixi
 * @since 1.0.0
 */
public class CreateResponse<PK> implements Serializable {

  private PK id;

  public CreateResponse() {
  }

  public CreateResponse(PK id) {
    this.id = id;
  }

  public PK getId() {
    return id;
  }

  public void setId(PK id) {
    this.id = id;
  }
}
