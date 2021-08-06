package org.yixi.thyme.core.util;

import org.yixi.thyme.core.Thyme;

/**
 * @author yixi
 * @since 1.1.3
 */
public class SnowFlake {

  private final long workerId;
  private long inc = 0L;
  private long incMask = 4095L;
  private long lastTick = -1L;

  public SnowFlake(long workerId) {
    if (workerId >= 1024) {
      throw Thyme.ex("workerId[%d] 必须小于 1024", workerId);
    }
    this.workerId = workerId;
  }

  public synchronized long nextId() {
    long tick = tick();
    if (tick < lastTick) {
      throw Thyme.ex("Clock moved backwards.  Refusing to generate id");
    } else {
      if (tick == lastTick) {
        inc = inc + 1L & incMask;
        if (inc == 0L) {
          tick = nextMillis(tick);
        }
      } else {
        inc = 0L;
      }
      lastTick = tick;
      return tick - 1480166465631L << 22 | workerId << 12 | inc;
    }
  }

  private long nextMillis(long lastTimestamp) {
    long timestamp = tick();
    while (timestamp <= lastTimestamp) {
      timestamp = tick();
    }
    return timestamp;
  }

  private long tick() {
    return System.currentTimeMillis();
  }
}
