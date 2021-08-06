package org.yixi.data.client;

import java.io.Serializable;

/**
 * @author yixi
 * @since 1.0.0
 */
public class DeleteResponse implements Serializable {

  private Integer amount;

  public DeleteResponse() {
  }

  public DeleteResponse(Integer amount) {
    this.amount = amount;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }
}
