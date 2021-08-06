package org.yixi.data.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class UpsertRequest<PK, Entity> implements Serializable {

  private Map<String, Object> filter;
  private Entity doc;

  public Map<String, Object> getFilter() {
    return filter;
  }

  public void setFilter(Map<String, Object> filter) {
    this.filter = filter;
  }

  public Entity getDoc() {
    return doc;
  }

  public void setDoc(Entity doc) {
    this.doc = doc;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Builder<PK, Entity> {

    private final UpsertRequest<PK, Entity> upsertRequest = new UpsertRequest();
    private final Map<String, Object> filter = new HashMap<>();

    public static Builder builder() {
      return new Builder();
    }


    public UpsertRequest<PK, Entity> build() {
      upsertRequest.setFilter(filter);
      return upsertRequest;
    }

    public Builder filter(Map<String, Object> filter) {
      this.filter.putAll(filter);
      return this;
    }

    public Builder filter(String key, Object value) {
      this.filter.put(key, value);
      return this;
    }

    public Builder doc(Entity doc) {
      upsertRequest.setDoc(doc);
      return this;
    }

  }
}
