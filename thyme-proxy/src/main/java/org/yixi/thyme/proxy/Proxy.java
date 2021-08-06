package org.yixi.thyme.proxy;

import org.yixi.thyme.proxy.RouterConfig.Operator;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface Proxy {

  void proxy(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException;

  void addListener(String location, Operator operator, ProxyEventListener listener);
}
