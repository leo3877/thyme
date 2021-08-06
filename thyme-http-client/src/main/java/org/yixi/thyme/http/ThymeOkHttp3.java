package org.yixi.thyme.http;

import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.ex.ForbiddenException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.ex.ThymeExceptions;
import org.yixi.thyme.core.ex.UnauthorizedException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.Assertions;
import org.yixi.thyme.http.HttpRequest.ObjectRequestBody;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Source;
import okio.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ThymeOkHttp3 implements ThymeHttpClient {

  private static final Logger logger = LoggerFactory.getLogger(ThymeOkHttp3.class);

  private final OkHttpClient okHttpClient;
  private final boolean throwThymeException;

  public ThymeOkHttp3(OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
    this.throwThymeException = false;
  }

  public ThymeOkHttp3(OkHttpClient okHttpClient, boolean throwThymeException) {
    this.okHttpClient = okHttpClient;
    this.throwThymeException = throwThymeException;
  }

  @Override
  public String get(String url) {
    return get(url, null, String.class);
  }

  @Override
  public String get(String url, Map<String, Object> headers) {
    return get(url, headers, String.class);
  }

  @Override
  public <T> T get(String url, Class<T> clazz) {
    return get(url, null, clazz);
  }

  @Override
  public <T> T get(String url, Map<String, Object> headers, Class<T> clazz) {
    return call(HttpRequest.get(url).addHeaders(headers), clazz);
  }

  @Override
  public String postForm(String url) {
    return postForm(url, (Map<String, Object>) null);
  }

  @Override
  public String postForm(String url, Map<String, Object> params) {
    return postForm(url, params, String.class);
  }

  @Override
  public String postForm(String url, Map<String, Object> headers, Map<String, Object> params) {
    return postForm(url, headers, params, String.class);
  }

  @Override
  public <T> T postForm(String url, Class<T> clazz) {
    return postForm(url, null, clazz);
  }

  @Override
  public <T> T postForm(String url, Map<String, Object> params, Class<T> clazz) {
    return postForm(url, null, params, clazz);
  }

  @Override
  public <T> T postForm(String url, Map<String, Object> headers, Map<String, Object> params,
    Class<T> clazz) {
    return call(HttpRequest.post(url).addHeaders(headers).formRequestBody(params), clazz);
  }

  @Override
  public String postJson(String url, Object obj) {
    return postJson(url, null, obj);
  }

  @Override
  public String postJson(String url, Map<String, Object> headers, Object obj) {
    return postJson(url, headers, obj, String.class);
  }

  @Override
  public <T> T postJson(String url, Object obj, Class<T> clazz) {
    return postJson(url, null, obj, clazz);
  }

  @Override
  public <T> T postJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz) {
    return call(build(url, HttpRequest.HttpMethod.POST, headers, obj), clazz);
  }

  @Override
  public String putJson(String url, Object obj) {
    return putJson(url, null, obj);
  }

  @Override
  public String putJson(String url, Map<String, Object> headers, Object obj) {
    return putJson(url, headers, obj, String.class);
  }

  @Override
  public <T> T putJson(String url, Object obj, Class<T> clazz) {
    return putJson(url, null, obj, clazz);
  }

  @Override
  public <T> T putJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz) {
    return call(build(url, HttpRequest.HttpMethod.PUT, headers, obj), clazz);
  }

  @Override
  public String patchJson(String url, Object obj) {
    return patchJson(url, null, obj);
  }

  @Override
  public String patchJson(String url, Map<String, Object> headers, Object obj) {
    return patchJson(url, headers, obj, String.class);
  }

  @Override
  public <T> T patchJson(String url, Object obj, Class<T> clazz) {
    return patchJson(url, null, obj, clazz);
  }

  @Override
  public <T> T patchJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz) {
    return call(build(url, HttpRequest.HttpMethod.PATCH, headers, obj), clazz);
  }

  @Override
  public String deleteJson(String url, Object obj) {
    return deleteJson(url, null, obj);
  }

  @Override
  public String deleteJson(String url, Map<String, Object> headers, Object obj) {
    return deleteJson(url, headers, obj, String.class);
  }

  @Override
  public <T> T deleteJson(String url, Object obj, Class<T> clazz) {
    return deleteJson(url, null, obj, clazz);
  }

  @Override
  public <T> T deleteJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz) {
    return call(build(url, HttpRequest.HttpMethod.DELETE, headers, obj), clazz);
  }

  @Override
  public String call(HttpRequest httpRequest) {
    return call(httpRequest, String.class);
  }

  @Override
  public <T> T call(HttpRequest httpRequest, Class<T> clazz) {
    Assertions.notNull("httpRequest", httpRequest);
    Assertions.notNull("url", httpRequest.getUrl());
    Assertions.notNull("clazz", clazz);

    Request.Builder builder = new Request.Builder().url(httpRequest.getUrl());
    httpRequest.getHeaders().forEach((k, v) -> {
      if (v instanceof List) {
        ((List) v).forEach(value -> builder.addHeader(k, value.toString()));
      } else {
        builder.addHeader(k, v.toString());
      }
    });
    String requestId = new ObjectId().toHexString();
    builder.addHeader("X-Thyme-Request-Id", requestId);
    HttpRequest.Body requestBody = httpRequest.getRequestBody();
    RequestBody okRequestBody = null;
    if (requestBody instanceof HttpRequest.FormRequestBody) {
      FormBody.Builder formBodyBuilder = new FormBody.Builder();
      ((HttpRequest.FormRequestBody) requestBody).getParams().forEach((k, v) -> {
        if (v instanceof List) {
          ((List) v).forEach(value -> formBodyBuilder.add(k, value.toString()));
        } else {
          formBodyBuilder.add(k, v.toString());
        }
      });
      okRequestBody = formBodyBuilder.build();
    } else if (requestBody instanceof HttpRequest.ObjectRequestBody) {
      Object content = ((HttpRequest.ObjectRequestBody) requestBody).getObject();
      String contentType = requestBody.getContentType();
      if (content instanceof String) {
        okRequestBody = RequestBody.create(MediaType.parse(contentType), (String) content);
      } else if (content instanceof byte[]) {
        okRequestBody = RequestBody.create(MediaType.parse(contentType), (byte[]) content);
      } else if (content instanceof File) {
        okRequestBody = RequestBody.create(MediaType.parse(contentType), (File) content);
      } else if (content instanceof InputStream) {
        okRequestBody = new RequestBody() {
          @Override
          public MediaType contentType() {
            return MediaType.parse(contentType);
          }

          @Override
          public long contentLength() throws IOException {
            return ((ObjectRequestBody) requestBody).getContentLength();
          }

          @Override
          public void writeTo(BufferedSink sink) throws IOException {
            InputStream in = (InputStream) content;

            sink.writeAll(new Source() {
              @Override
              public long read(Buffer sink, long byteCount) throws IOException {
                if (sink == null) {
                  throw new IllegalArgumentException("sink == null");
                }
                if (byteCount < 0) {
                  throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                }
                byte[] bytes = new byte[(int) byteCount];
                int len = in.read(bytes);
                if (len > 0) {
                  sink.write(bytes, 0, len);
                }
                return len;
              }

              @Override
              public Timeout timeout() {
                return Timeout.NONE;
              }

              @Override
              public void close() throws IOException {
                in.close();
              }
            });
          }
        };
      }
    } else if (requestBody instanceof HttpRequest.MultipartFormRequestBody) {
      Map<String, Object> params = ((HttpRequest.MultipartFormRequestBody) requestBody)
        .getMultiparts();
      MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(
        MediaType.parse(requestBody.getContentType()));
      params.forEach((k, v) -> {
        if (v instanceof File) {
          String name = ((File) v).getName();
          multipartBodyBuilder
            .addFormDataPart(k, name, RequestBody.create(MediaType.parse("file/*"), (File) v));
        } else {
          multipartBodyBuilder.addFormDataPart(k, v.toString());
        }
      });
    }
    builder.method(httpRequest.getMethod().name(), okRequestBody);
    Call call = okHttpClient.newCall(builder.build());
    try {
      Response response = call.execute();
      ResponseBody body = response.body();
      if (throwThymeException && (200 > response.code() || response.code() >= 400)) {
        String errorMsg = body.string();
        log(requestId, httpRequest, response, errorMsg);
        if ("json".equals(body.contentType().subtype())) {
          throw ThymeExceptions.from(Jsons.decode(errorMsg, Map.class));
        } else if (response.code() == 401) {
          throw new UnauthorizedException(errorMsg);
        } else if (response.code() == 403) {
          throw new ForbiddenException(errorMsg);
        } else if (response.code() == 404) {
          throw new ThymeException("404，你访问的地址不存在: " + call.request().url());
        } else {
          throw new ThymeException(
            "rawResponse: " + response.toString() + ", errorBody: " + errorMsg);
        }
      }
      T obj;
      com.google.common.net.MediaType mediaType = null;
      if (body.contentType() != null) {
        mediaType = com.google.common.net.MediaType.parse(body.contentType().toString());
      }
      if (ThymeResponse.class == clazz) {
        ThymeResponse thymeResponse = new ThymeResponse();
        thymeResponse.setRequest(httpRequest);
        thymeResponse.setDataStream(body.byteStream());
        thymeResponse.setContentLength(body.contentLength());
        thymeResponse.setCode(response.code());
        Headers headers = response.headers();
        HttpRequest.Multimap newHeaders = new HttpRequest.Multimap();
        for (int i = 0, size = headers.size(); i < size; i++) {
          newHeaders.add(headers.name(i), headers.value(i));
        }
        thymeResponse.setHeaders(newHeaders);
        thymeResponse.setResponseTime(-1); // TODO
        if (body.contentType() != null) {
          thymeResponse.setMediaType(mediaType);
        }
        obj = (T) thymeResponse;
      } else {
        obj = ThymeHttpClient.read(httpRequest, body.byteStream(), body.contentLength(),
          mediaType, clazz);
      }
      if (logger.isDebugEnabled()) {
        log(requestId, httpRequest, response, obj);
      }
      return obj;
    } catch (IOException e) {
      Request request = call.request();
      throw new ThymeException(
        String.format("%s executing %s %s", e.getMessage(), request.method(),
          request.url()), e);
    }
  }

  private HttpRequest build(String url, HttpRequest.HttpMethod method,
    Map<String, Object> headers, Object obj) {
    HttpRequest.ObjectRequestBody requestBody;
    if (obj instanceof byte[]) {
      requestBody = HttpRequest.Body.jsonBody((byte[]) obj);
    } else if (obj instanceof String) {
      requestBody = HttpRequest.Body.jsonBody((String) obj);
    } else {
      requestBody = HttpRequest.Body.jsonBody(Jsons.encode(obj));
    }
    return HttpRequest.create(url, method).addHeaders(headers).requestBody(requestBody);
  }

  private void log(String requestId, HttpRequest request, Response response,
    Object res) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n")
      .append("General:").append("\n")
      .append("Request Url: ").append(request.getUrl()).append("\n")
      .append("Request Method: ").append(request.getMethod()).append("\n")
      .append("Status Code: ").append(response.code()).append("\n")
      .append("\n")
      .append("Request Headers").append("\n")
      .append(RequestHeaderEventListener.getRequestHeaders(requestId))
      .append("\n")
      .append("Request Body").append("\n");

    HttpRequest.Body requestBody = request.getRequestBody();
    if (requestBody instanceof HttpRequest.FormRequestBody) {
      HttpRequest.Multimap params = ((HttpRequest.FormRequestBody) requestBody).getParams();
      sb.append(params.getAdapter().toString()).append("\n");
    } else if (requestBody instanceof HttpRequest.ObjectRequestBody) {
      Object object = ((HttpRequest.ObjectRequestBody) requestBody).getObject();
      if (object instanceof String) {
        sb.append(object).append("\n");
      } else if (object instanceof File) {
        sb.append("file: " + ((File) object).getPath()).append("\n");
      } else if (object instanceof byte[]) {
        sb.append("bytes, length: " + ((byte[]) object).length).append("\n");
      }
    } else {
      sb.append("[无]").append("\n");
    }
    sb.append("\n");
    sb.append("Response Headers").append("\n")
      .append(response.headers()).append("\n");

    sb.append("Response Body").append("\n");
    if (res instanceof InputStream) {
      sb.append("[InputStream]").append("\n");
    } else if (res instanceof byte[]) {
      sb.append("[bytes]").append("\n");
    } else if (res instanceof String) {
      sb.append(res).append("\n");
    } else if (res instanceof ThymeResponse) {
      sb.append("[ThymeResponse]").append("\n");
    } else {
      sb.append(Jsons.encode(res)).append("\n");
    }
    if (logger.isDebugEnabled()) {
      logger.debug(sb.toString());
    }
    RequestHeaderEventListener.removeRequestHeaders(requestId);
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Builder {

    private final OkHttpClient.Builder builder;
    private boolean throwThymeException; // 当 status < 200 || status > 400 时是否抛thyme 定义的标准异常

    private ThymeProxySelector thymeProxySelector = null;

    public Builder() {
      builder = new OkHttpClient.Builder();
      debugRequestHeader();
    }

    public static Builder builder() {
      return new Builder();
    }

    // 是否超时重试
    public Builder timeoutRetry(boolean f) {
      if (f) {
        builder.addInterceptor(new RetryInterceptor());
      }
      return this;
    }

    // 是否超时重试
    public Builder throwThymeException() {
      throwThymeException = true;
      return this;
    }

    // 是否进行链路跟踪
    public Builder trace(boolean f) {
      if (f) {
        builder.addInterceptor(new OkHttpTraceInterceptor());
      }
      return this;
    }

    public Builder addInterceptor(Interceptor interceptor) {
      builder.addInterceptor(interceptor);
      return this;
    }

    // 不验证 https 证书
    public Builder httpsNoVerify() {
      try {
        SSLContext sc = SSLContext.getInstance("TLS");
        X509TrustManager x509TrustManager = new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }

          @Override
          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        };
        sc.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        builder.sslSocketFactory(sc.getSocketFactory(), x509TrustManager);
        builder.hostnameVerifier((s, sslSession) -> true);
      } catch (Exception e) {
        // ignore
      }
      return this;
    }

    // 建立连接超时时间
    public Builder connectTimeout(Integer connectTimeout) {
      builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
      return this;
    }

    // 请求超时时间
    public Builder readTimeout(Integer readTimeout) {
      builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
      return this;
    }

    // http 重定向
    public Builder followRedirects(boolean f) {
      builder.followRedirects(f);
      return this;
    }

    // https 重定向
    public Builder followSslRedirects(boolean f) {
      builder.followSslRedirects(f);
      return this;
    }

    // 写入超时时间
    public Builder writeTimeout(Integer writeTimeout) {
      builder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
      return this;
    }

    // 是否自动管理 Cookie
    public Builder cookie(boolean isCookie) {
      if (isCookie) {
        builder.cookieJar(new OkHttpCookieManager());
      }
      return this;
    }

    /**
     * @param host 制定需要代理的 url，可以为空，表示所有请求都要走代理
     * @param type 代理类型 HTTP, SOCKS
     * @param proxyHost 代理服务器 ip
     * @param port 代理服务器端口
     */
    public Builder addProxyHost(String host, Proxy.Type type, String proxyHost, Integer port) {
      if (thymeProxySelector == null) {
        thymeProxySelector = new ThymeProxySelector();
      }
      thymeProxySelector.addProxyHost(host, type, proxyHost, port);
      return this;
    }

    private Builder debugRequestHeader() {
      if (logger.isDebugEnabled()) {
        builder.eventListener(new RequestHeaderEventListener());
      }
      return this;
    }

    private Builder thymeProxySelector() {
      if (thymeProxySelector != null) {
        builder.proxySelector(thymeProxySelector);
      }
      return this;
    }

    public ThymeOkHttp3 build() {
      debugRequestHeader().thymeProxySelector();
      return new ThymeOkHttp3(builder.build(), throwThymeException);
    }
  }
}
