package org.yixi.data.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author yixi
 * @since 1.0.0
 */
@Data
public class QueryResponse<T> implements Serializable {

  /**
   * 满足查询条件的总条目数
   */
  private Long amount;
  private Integer skip;
  private Integer limit;
  /**
   * 当前返回结果数
   */
  private Integer size;
  private List<T> objects = new ArrayList<>();

}
