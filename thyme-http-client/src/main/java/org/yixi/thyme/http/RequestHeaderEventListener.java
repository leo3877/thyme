package org.yixi.thyme.http;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Headers;
import okhttp3.Request;

/**
 * @author yixi
 * @since 1.0.0
 */
public class RequestHeaderEventListener extends EventListener {

  private static final ConcurrentMap<String, Headers> requestHeaders = new ConcurrentHashMap<>();

  @Override
  public void requestHeadersEnd(Call call, Request request) {
    String requestId = request.headers().get("X-Thyme-Request-Id");
    if (requestId != null) {
      requestHeaders.put(requestId, request.headers());
    }
  }

  public static Headers getRequestHeaders(String requestId) {
    return requestHeaders.get(requestId);
  }

  public static void removeRequestHeaders(String requestId) {
    requestHeaders.remove(requestId);
  }
}
