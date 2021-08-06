package org.yixi.thyme.proxy;

import org.yixi.thyme.proxy.RouterConfig.Operator;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ProxyFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(ProxyFilter.class);

  private final Proxy proxy = new OkHttp3Proxy();

  public ProxyFilter() {
  }

  public void addProxyRouter(String location, Operator operator, String host) {
    Routers.addProxyRouter(location, operator, host);
  }

  public void addProxyRouter(String location, boolean keep, Operator operator, String host) {
    Routers.addProxyRouter(location, keep, operator, host);
  }

  public void addRedirectRouter(String location, Operator operator) {
    Routers.addRedirectRouter(location, operator);
  }

  public void addListener(String location, Operator operator, ProxyEventListener listener) {
    proxy.addListener(location, operator, listener);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    logger.info("ProxyFilter init start");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    proxy.proxy(chain, (HttpServletRequest) request, (HttpServletResponse) response);
  }

  @Override
  public void destroy() {
  }


}
