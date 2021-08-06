package org.yixi.thyme.proxy;

import org.yixi.thyme.http.HttpRequest;
import org.yixi.thyme.http.ThymeHttpClient.ThymeResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface ProxyEventListener {

  HttpServletRequest preRequest(HttpServletRequest httpServletRequest, HttpRequest proxyRequest);

  void postResponse(HttpServletRequest httpServletRequest, HttpRequest proxyRequest,
    ThymeResponse thymeResponse);
}
