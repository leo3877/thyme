package org.yixi.thyme.data.mongo;

import org.yixi.thyme.data.Constants;
import lombok.Data;

/**
 * @author yixi
 * @since 1.1.2
 */
@Data
public class MongoClientConfig {

  private String urls;
  private String database;
  private String readPreference;
  /**
   * 查询超时时间，默认 6 秒
   */
  private int queryTimeout = Constants.DEFAULT_QUERY_TIMEOUT;

  private String username;
  private String password;
  private String authDatabase;
}
