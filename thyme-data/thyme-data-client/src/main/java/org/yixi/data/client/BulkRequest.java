package org.yixi.data.client;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.yixi.thyme.core.BaseEntity;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class BulkRequest<Entity extends BaseEntity> implements Serializable {

  /**
   * 默认非事务操作
   */
  private boolean transactional = false;

  private List<Entity> creates = new LinkedList<>();
  private List<Entity> updates = new LinkedList<>();
  private List<Entity> replaces = new LinkedList<>();
  /**
   * ID 列表，根据 ID 进行删除
   */
  private List<Object> deletes = new LinkedList<>();

  public boolean isTransactional() {
    return transactional;
  }

  public void setTransactional(boolean transactional) {
    this.transactional = transactional;
  }

  public List<Entity> getCreates() {
    return creates;
  }

  public void setCreates(List<Entity> creates) {
    this.creates = creates;
  }

  public List<Entity> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Entity> updates) {
    this.updates = updates;
  }

  public List<Entity> getReplaces() {
    return replaces;
  }

  public void setReplaces(List<Entity> replaces) {
    this.replaces = replaces;
  }

  public List<Object> getDeletes() {
    return deletes;
  }

  public void setDeletes(List<Object> deletes) {
    this.deletes = deletes;
  }

  public int size() {
    return creates.size() + updates.size() + replaces.size() + deletes.size();
  }
}
