package org.yixi.thyme.proxy;

import org.yixi.thyme.core.matcher.Operator;
import org.yixi.thyme.core.matcher.Pattern;
import org.yixi.thyme.proxy.RouterConfig.Route;

/**
 * @author yixi
 * @since 1.0.0
 */
public class Router {

  private final RouterConfig routerConfig;
  private final Pattern pattern;

  public Router(RouterConfig routerConfig) {
    this.routerConfig = routerConfig;
    RouterConfig.Operator operator = routerConfig.getOperator();
    if (operator == RouterConfig.Operator.regex) {
      pattern = Pattern.parse(Operator.regex, routerConfig.getLocation());
    } else if (operator == RouterConfig.Operator.eq) {
      pattern = Pattern.parse(Operator.eq, routerConfig.getLocation());
    } else if (operator == RouterConfig.Operator.antPath) {
      pattern = Pattern.parse(Operator.antPath, routerConfig.getLocation());
    } else if (operator == RouterConfig.Operator.startsWith) {
      pattern = Pattern.parse(Operator.startsWith, routerConfig.getLocation());
    } else if (operator == RouterConfig.Operator.endsWith) {
      pattern = Pattern.parse(Operator.endsWith, routerConfig.getLocation());
    } else if (operator == RouterConfig.Operator.contains) {
      pattern = Pattern.parse(Operator.contains, routerConfig.getLocation());
    } else {
      throw new UnsupportedOperationException("router config operator: " + operator);
    }
  }

  public Route route(String uri) {
    if (pattern.match(uri)) {
      return routerConfig.getRoute();
    } else {
      return null;
    }
  }

}
