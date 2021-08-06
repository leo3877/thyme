package org.yixi.data.client;

import java.io.Serializable;

/**
 * @author yixi
 * @since 1.0.0
 */
public class UpdateResponse implements Serializable {

  private long matched;

  private long modified;

  public UpdateResponse() {
  }

  public UpdateResponse(long matched, long modified) {
    this.matched = matched;
    this.modified = modified;
  }

  public long getMatched() {
    return matched;
  }

  public void setModified(long modified) {
    this.modified = modified;
  }

  public long getModified() {
    return modified;
  }

  public void setMatched(long matched) {
    this.matched = matched;
  }
}
