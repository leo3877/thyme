package org.yixi.thyme.proxy;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.proxy.RouterConfig.Operator;
import org.yixi.thyme.proxy.RouterConfig.Route;
import org.yixi.thyme.proxy.RouterConfig.Type;

/**
 * @author yixi
 * @since 1.0.0
 */
public class Routers {

  private static final Logger logger = LoggerFactory.getLogger(Routers.class);

  private final static List<Router> routers = new ArrayList<>();

  public static void addProxyRouter(String location, Operator operator, String host) {
    addRouter(RouterConfig.builder()
      .location(location)
      .operator(operator)
      .route(Route.builder().type(Type.Proxy).value(host).build()).build());
  }

  public static void addProxyRouter(String location, boolean keep, Operator operator, String host) {
    addRouter(RouterConfig.builder()
      .location(location)
      .operator(operator)
      .route(Route.builder().type(Type.Proxy).keep(keep).location(location).value(host).build())
      .build());
  }

  public static void addRedirectRouter(String location, Operator operator) {
    addRouter(RouterConfig.builder()
      .location(location)
      .operator(operator)
      .route(Route.builder().type(Type.Redirect).build()).build());
  }

  public static void addRouter(RouterConfig routerConfig) {
    logger.info("addRouter: " + Jsons.encodePretty(routerConfig));
    routers.add(new Router(routerConfig));
  }

  public static Route route(String uri) {
    for (Router router : routers) {
      Route route = router.route(uri);
      if (route != null) {
        return route;
      }
    }
    return null;
  }

}
