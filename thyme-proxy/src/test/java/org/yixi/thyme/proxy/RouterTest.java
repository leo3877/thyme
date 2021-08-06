package org.yixi.thyme.proxy;

import org.yixi.thyme.proxy.RouterConfig.Operator;
import org.junit.Test;

/**
 * @author yixi
 * @since 1.0.0
 */
public class RouterTest {

  @Test
  public void test() {
    Routers.addProxyRouter("/test", Operator.startsWith, "http://localhost:8080");
  }
}
