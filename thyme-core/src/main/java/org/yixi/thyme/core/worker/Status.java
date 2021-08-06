package org.yixi.thyme.core.worker;

/**
 * @author yixi
 */
public enum Status {

  /**
   * 可运行状态
   */
  Runnable,

  /**
   * 启动中
   */
  Starting,

  /**
   * 运行中
   */
  Running,

  /**
   * 停止中
   */
  Stopping,

  /**
   * 已停止
   */
  Stopped,

  /**
   * 服务当机，会主动恢复
   */
  Downed,

  /**
   * 服务运行错误，无法恢复(如发生无法恢复的异常等情况，如程序逻辑错误)
   */
  Failed

}