package org.yixi.thyme.core.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.yixi.thyme.core.Retryer;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.core.util.Randoms;
import org.yixi.thyme.core.worker.AbstractLifeCycle;

/**
 * 周期性任务，遇到异常不断重试，永不停止
 *
 * @author yixi
 */
@Slf4j
public abstract class AbstractRetryForeverIntervalExecutor extends AbstractLifeCycle {

  private final String name;
  private final Retryer retryer;
  private final long intervalMin;
  private final long intervalMax;

  private volatile Thread executor;

  public AbstractRetryForeverIntervalExecutor(String name, Retryer retryer, long intervalMin,
    long intervalMax) {
    this.name = name;
    this.retryer = retryer;
    this.intervalMin = intervalMin;
    this.intervalMax = intervalMax;
  }


  @Override
  public void doStart() {
    this.executor = new Thread(this::start0, name);
    executor.start();
    log.info("{} start successful.", name);
  }

  private void start0() {
    Thyme.retryForever(retryer, () -> {
      try {
        while (true) {
          if (!isRunning() && !isStarting()) {
            return null;
          }
          long start = System.currentTimeMillis();
          doStart0();
          long end = System.currentTimeMillis();
          long interval = Randoms.nextLong(intervalMin, intervalMax);
          if (interval - (end - start) > 0) {
            Thread.sleep(interval);
          }
        }
      } catch (InterruptedException e) {
        log.info("{} Interrupted", this);
        return null;
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        throw new RetryableException(e.getMessage(), e);
      }
    });
  }

  public abstract void doStart0();

}
