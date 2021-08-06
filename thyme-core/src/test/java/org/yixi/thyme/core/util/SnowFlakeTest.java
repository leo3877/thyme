package org.yixi.thyme.core.util;

public class SnowFlakeTest {

  private final SnowFlake snowFlake = new SnowFlake(0L);

  public void testNextId() {
    long result = snowFlake.nextId();
  }
}
