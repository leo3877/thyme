package org.yixi.thyme.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.http.HttpRequest;
import org.yixi.thyme.http.HttpRequest.Body;
import org.yixi.thyme.http.HttpRequest.HttpMethod;
import org.yixi.thyme.http.ThymeHttpClient;
import org.yixi.thyme.http.ThymeHttpClient.ThymeResponse;
import org.yixi.thyme.http.ThymeOkHttp3;
import org.yixi.thyme.proxy.RouterConfig.Operator;
import org.yixi.thyme.proxy.RouterConfig.Route;
import org.yixi.thyme.proxy.RouterConfig.Type;

/**
 * @author yixi
 * @since 1.0.0
 */
public class OkHttp3Proxy implements Proxy {

  private static final Logger logger = LoggerFactory.getLogger(OkHttp3Proxy.class);

  private final List<ProxyEventListenerWrapper> eventListenerWrappers = new ArrayList<>();

  private final ThymeHttpClient thymeHttpClient = ThymeOkHttp3.Builder.builder()
    .connectTimeout(2000)
    .writeTimeout(60 * 1000)
    .readTimeout(60 * 1000)
    .httpsNoVerify()
    .build();

  private final HeaderCopier headerCopier = new HeaderCopier();

  @Override
  public void proxy(FilterChain filterChain, HttpServletRequest request,
    HttpServletResponse response)
    throws IOException, ServletException {
    String requestURI = request.getRequestURI();
    Route route = Routers.route(requestURI);
    if (route == null) {
      filterChain.doFilter(request, response);
      return;
    }
    List<ProxyEventListener> proxyEventListeners = proxyEventListeners(requestURI);
    String queryString = request.getQueryString();
    if (queryString != null) {
      requestURI += "?" + queryString;
    }
    String url;
    if (route.getType() == Type.Proxy) {
      url = route.getValue() + requestURI;
      if (!route.isKeep()) {
        url = url.replace(route.getLocation(), "");
      }
    } else if (route.getType() == Type.Redirect) {
      String targetUrl = request.getParameter("targetUrl");
      if (targetUrl == null) {
        throw new ThymeException("targetUrl can not be null");
      }
      url = targetUrl;
    } else {
      throw new ThymeException("Unsupported route type: " + route.getType());
    }
    HttpRequest proxyRequest = HttpRequest
      .create(url, HttpMethod.valueOf(request.getMethod().toUpperCase()));
    String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
    if (contentType == null) {
      contentType = "text/html; charset=utf-8";
    }

    if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null
      || request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
      proxyRequest.requestBody(
        Body.objectBody(contentType, request.getInputStream(), getContentLength(request)));
    }
    headerCopier.copyRequestHeaders(request, proxyRequest);
    if (!proxyEventListeners.isEmpty()) {
      for (ProxyEventListener listener : proxyEventListeners) {
        request = listener.preRequest(request, proxyRequest);
      }
    }
    if (request instanceof MultipleReadHttpRequest) {
      proxyRequest.requestBody(
        Body.objectBody(contentType, request.getInputStream(), getContentLength(request)));
      request.getInputStream().mark(10 * 1024 * 1024);
    }
    ThymeResponse thymeResponse = thymeHttpClient.call(proxyRequest, ThymeResponse.class);
    if (request instanceof MultipleReadHttpRequest) {
      request.getInputStream().reset();
    }
    if (!proxyEventListeners.isEmpty()) {
      for (ProxyEventListener listener : proxyEventListeners) {
        listener.postResponse(request, proxyRequest, thymeResponse);
      }
    }
    response.setStatus(thymeResponse.getCode());
    headerCopier.copyResponseHeaders(thymeResponse, response);
    writeToResponse(thymeResponse, response);
  }

  private void writeToResponse(ThymeResponse thymeResponse, HttpServletResponse response)
    throws IOException {
    IOUtils.copy(thymeResponse.getDataStream(), response.getOutputStream());
  }


  private int getContentLength(HttpServletRequest request) {
    String contentLengthHeader = request.getHeader(HttpHeaders.CONTENT_LENGTH);
    if (contentLengthHeader != null) {
      return Integer.parseInt(contentLengthHeader);
    }
    return -1;
  }

  @Override
  public void addListener(String location, Operator operator, ProxyEventListener listener) {
    eventListenerWrappers.add(ProxyEventListenerWrapper.builder()
      .listener(listener)
      .router(new Router(RouterConfig.builder()
        .location(location)
        .operator(operator)
        .route(Route.builder().type(Type.Script).build()).build())).build());
  }

  private List<ProxyEventListener> proxyEventListeners(String uri) {
    List<ProxyEventListener> matches = new ArrayList<>();
    for (ProxyEventListenerWrapper eventListenerWrapper : eventListenerWrappers) {
      if (eventListenerWrapper.getRouter().route(uri) != null) {
        matches.add(eventListenerWrapper.getListener());
      }
    }
    return matches;
  }


  /**
   * @author yixi
   */
  public static class HeaderCopier {

    /**
     * "hop-by-hop" headers that should not be copied. http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
     */
    private final Map<String, Object> hopByHopHeaders = new HashMap<>();

    public HeaderCopier() {
      hopByHopHeaders.put("Connection".toLowerCase(), 1);
      hopByHopHeaders.put("Keep-Alive".toLowerCase(), 1);
      hopByHopHeaders.put("Proxy-Authenticate".toLowerCase(), 1);
      hopByHopHeaders.put("Proxy-Authorization".toLowerCase(), 1);
      hopByHopHeaders.put("TE".toLowerCase(), 1);
      hopByHopHeaders.put("Trailers".toLowerCase(), 1);
      hopByHopHeaders.put("Transfer-Encoding".toLowerCase(), 1);
      hopByHopHeaders.put("Upgrade".toLowerCase(), 1);
    }

    void copyRequestHeaders(HttpServletRequest request, HttpRequest proxyRequest) {
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
          || hopByHopHeaders.get(headerName.toLowerCase()) != null) {
          continue; // ignore content-length and hop by hop headers
        }
        if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
          // TODO
        } else {
          proxyRequest.addHeader(headerName, request.getHeader(headerName));
        }
      }
    }

    void copyResponseHeaders(ThymeResponse thymeResponse, HttpServletResponse response) {
      thymeResponse.getHeaders().forEach((k, v) -> {
        if ("Date".equalsIgnoreCase(k)) {
          response.setHeader(k, v.toString());
        } else if (v instanceof List) {
          ((List) v).forEach(vv -> response.addHeader(k, vv.toString()));
        } else {
          response.addHeader(k, v.toString());
        }
      });
    }
  }

  /**
   * @author yixi
   */
  @Data
  @Builder
  public static class ProxyEventListenerWrapper {

    private ProxyEventListener listener;
    private Router router;
  }
}
