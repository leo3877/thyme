package org.yixi.thyme.core;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.yixi.thyme.core.ex.RetryableException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重试机制
 *
 * @author baitouweng
 */
public interface Retryer {

  Retryer copy();

  <T> T retry(Supplier<T> supplier);

  /**
   * 重试机制的默认实现。
   */
  class Default implements Retryer {

    private static final Logger logger = LoggerFactory.getLogger(Retryer.class);

    private final int maxAttempts;
    private final long period;
    private final long maxPeriod;
    int attempt;
    long sleptForMillis;

    public Default() {
      this(3);
    }

    public Default(int maxAttempts) {
      this(100, SECONDS.toMillis(2), maxAttempts);
    }

    public Default(long period, long maxPeriod, int maxAttempts) {
      this.period = period;
      this.maxPeriod = maxPeriod;
      this.maxAttempts = maxAttempts;
      this.attempt = 1;
    }

    protected long currentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    public Retryer copy() {
      return new Default(period, maxPeriod, maxAttempts);
    }

    @Override
    public <T> T retry(Supplier<T> supplier) {
      while (true) {
        try {
          return supplier.get();
        } catch (RetryableException e) {
          continueOrPropagate(e);
        }
      }
    }

    private void continueOrPropagate(RetryableException e) {
      if (attempt++ >= maxAttempts) {
        throw new RetryableException(
          String.format("[Retry failure, attempt %d times, consume %d ] %s millis", maxAttempts,
            sleptForMillis, e.getMessage()), e.getCause());
      }
      long interval;
      if (e.retryAfter() != null) {
        interval = e.retryAfter().getTime() - currentTimeMillis();
        if (interval > maxPeriod) {
          interval = maxPeriod;
        }
        if (interval < 0) {
          return;
        }
      } else {
        interval = nextMaxInterval();
      }
      logger.error("Retry attempts " + attempt + " , after " + interval);
      try {
        Thread.sleep(interval);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      sleptForMillis += interval;
    }


    long nextMaxInterval() {
      long interval = (long) (period * Math.pow(1.5, attempt - 1));
      return interval > maxPeriod ? maxPeriod : interval;
    }
  }
}
