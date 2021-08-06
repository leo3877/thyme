package org.yixi.data.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * REST 接口批量操作返回结果。<br> 返回示例：
 * <pre>
 * {
 *   "creates": [
 *      {
 *        "id": "595e2dc181532a6cc96686cc",
 *      }
 *      {
 *     "code": 9000,
 *     "message": "数据格式错误"
 *   }],
 *   "updates": [{
 *     "code": 9000,
 *     "message": "id must be specified can not be null"
 *   }, {
 *     "amount": 1
 *   }],
 *   "deletes": [{
 *     "amount": 1
 *   }]
 * }
 * </pre>
 *
 * @author yixi
 * @since 1.0.0
 */
public class BulkResponse implements Serializable {

  private List<Object> creates = new ArrayList<>();
  private List<Object> updates = new ArrayList<>();
  private List<Object> replaces = new ArrayList<>();
  private Object deletes = new HashMap<>();

  public BulkResponse() {
  }

  public List<Object> getCreates() {
    return creates;
  }

  public void setCreates(List<Object> creates) {
    this.creates = creates;
  }

  public List<Object> getReplaces() {
    return replaces;
  }

  public void setReplaces(List<Object> replaces) {
    this.replaces = replaces;
  }

  public List<Object> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Object> updates) {
    this.updates = updates;
  }

  public Object getDeletes() {
    return deletes;
  }

  public void setDeletes(Object deletes) {
    this.deletes = deletes;
  }
}
