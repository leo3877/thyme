package org.yixi.thyme.data.mongo.tail;

import java.util.function.Consumer;
import org.bson.BsonTimestamp;

/**
 * 实时 tail 数据源的 oplog（即操作日志） 日志<br> 具体实现应该满足：<br> 1. 目标数据源服务从异常恢复能够自动感知并恢复 tail<br> 2. 异常恢复 tail 会调用
 * whenStart，方便业务逻辑处理一些因异常停止清理逻辑<br> 3. 用户监听函数逻辑出现异常时，中断该函数的后续 tail<br>
 *
 * @author yixi
 * @since 1.2.0
 */
public interface OplogTailer<T extends OplogTailer.Oplog> {

  /**
   * 设置开始监听日志的时间点
   */
  void setCheckPoint(BsonTimestamp checkPoint);

  /**
   * 启动 tailer 服务
   */
  void start();

  /**
   * 停止 tailer 服务
   */
  void stop();

  /**
   * tailer 运行状态
   */
  Integer status();

  BsonTimestamp currentCheckPoint();

  /**
   * 监听整个集群的 oplog
   */
  void addOplogListener(Consumer<T> fn);

  /**
   * 监听整个集群的 集群下某个 database 的所有 oplog
   */
  void addOplogListener(String database, Consumer<T> fn);

  void addOplogListener(String database, String table, Consumer<T> fn);

  /**
   * 删除整个集群的 oplog
   */
  void removeOplogListener(Consumer<T> fn);

  /**
   * 删除整个集群的 集群下某个 database 的所有 oplog
   */
  void removeOplogListener(String database, Consumer<T> fn);

  void removeOplogListener(String database, String table, Consumer<T> fn);

  /**
   * 启动的时候会调此方法
   */
  void whenStart(Consumer<BsonTimestamp> fn);

  void whenStop(Consumer<BsonTimestamp> fn);

  /**
   * 数据存储系统的操作日志
   *
   * @author yixi
   * @since 1.2.1
   */
  class Oplog {

  }

  /**
   * 操作日志的指令类型
   *
   * @author yixi
   * @since 1.2.1
   */
  @SuppressWarnings("all")
  public enum Op {
    n, // 系统指令

    c, // cmd 指令, 如创建索引，drop table 等

    i, // insert 指令, 插入数据

    u, // update 指令, 更新数据

    d // delete 指令, 删除数据
  }
}
