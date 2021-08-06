package org.yixi.thyme.http;

import com.google.common.collect.Lists;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.Assertions;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author yixi
 * @since 1.0.0
 */
public class HttpRequest {

  private String url;
  private HttpMethod method;

  private Multimap headers = new Multimap();
  private HttpRequest.Body requestBody;

  private Options options;

  public static HttpRequest get(String url) {
    return new HttpRequest(url, HttpMethod.GET);
  }

  public static HttpRequest post(String url) {
    return new HttpRequest(url, HttpMethod.POST);
  }

  public static HttpRequest delete(String url) {
    return new HttpRequest(url, HttpMethod.DELETE);
  }

  public static HttpRequest put(String url) {
    return new HttpRequest(url, HttpMethod.PUT);
  }

  public static HttpRequest patch(String url) {
    return new HttpRequest(url, HttpMethod.PATCH);
  }

  public static HttpRequest head(String url) {
    return new HttpRequest(url, HttpMethod.HEAD);
  }

  public static HttpRequest create(String url, HttpMethod method) {
    return new HttpRequest(url, method);
  }

  public HttpRequest() {

  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  private HttpRequest(String url, HttpMethod method) {
    Assertions.notNull("url", url);
    Assertions.notNull("method", method);
    this.url = url;
    this.method = method;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public HttpRequest setHeader(String name, String value) {
    headers.set(name, value);
    return this;
  }

  public HttpRequest addHeader(String name, String value) {
    headers.add(name, value);
    return this;
  }

  public HttpRequest removeHeader(String name) {
    headers.remove(name);
    return this;
  }

  public HttpRequest removeHeader(String name, String value) {
    headers.remove(name, value);
    return this;
  }

  public HttpRequest addHeaders(Map<String, Object> headers) {
    if (headers != null) {
      this.headers.addAll(headers);
    }
    return this;
  }

  public HttpRequest setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public Multimap getHeaders() {
    return headers;
  }

  public Options getOptions() {
    return options;
  }

  public HttpRequest requestBody(Body requestBody) {
    this.requestBody = requestBody;
    return this;
  }

  public HttpRequest jsonRequestBody(String json) {
    this.requestBody = Body.jsonBody(json);
    return this;
  }

  public HttpRequest jsonRequestBody(byte[] bytes) {
    this.requestBody = Body.jsonBody(bytes);
    return this;
  }

  public HttpRequest jsonRequestBody(Object obj) {
    this.requestBody = Body.jsonBody(Jsons.encode(obj));
    return this;
  }

  public HttpRequest formRequestBody(Map<String, Object> params) {
    this.requestBody = Body.formBody(params);
    return this;
  }

  public HttpRequest.Body getRequestBody() {
    return requestBody;
  }

  public HttpRequest copy() {
    HttpRequest copy = create(url, method);
    copy.headers = headers;
    copy.requestBody = requestBody;
    copy.options = options;
    return copy;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Multimap {

    private Map<String, Object> adapter = new HashMap<>();

    public void set(String name, Object value) {
      adapter.put(name, value);
    }

    public void add(String name, Object value) {
      Object pre = adapter.putIfAbsent(name, value);
      if (pre instanceof List) {
        ((List) pre).add(value);
      } else if (pre != null) {
        adapter.put(name, Lists.newArrayList(pre, value));
      }
    }

    public void add(String name, List<Object> values) {
      if (values != null) {
        values.forEach(v -> add(name, v));
      }
    }

    public void remove(String name) {
      adapter.remove(name);
    }

    public void remove(String name, Object value) {
      Object v = adapter.get(name);
      if (v instanceof List) {
        ((List) v).remove(value);
      } else {
        adapter.remove(name);
      }
    }

    public void addAll(Map<String, Object> map) {
      map.forEach(adapter::put);
    }

    /**
     * 返回 string 或者 List
     */
    public <T> T get(String name) {
      return (T) adapter.get(name);
    }

    public void forEach(BiConsumer<String, Object> action) {
      adapter.forEach(action::accept);
    }

    public Map<String, Object> getAdapter() {
      return adapter;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Options {

  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public enum HttpMethod {
    GET,

    HEAD,

    POST,

    PUT,

    PATCH,

    DELETE
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public abstract static class Body {

    protected String contentType;

    protected Body(String contentType) {
      this.contentType = contentType;
    }

    public String getContentType() {
      return contentType;
    }

    public static FormRequestBody formBody() {
      return new FormRequestBody();
    }

    public static FormRequestBody formBody(Map<String, Object> params) {
      return new FormRequestBody(params);
    }

    public static ObjectRequestBody jsonBody(String content) {
      return new ObjectRequestBody("application/json", content);
    }

    public static ObjectRequestBody jsonBody(byte[] bytes) {
      return new ObjectRequestBody("application/json", bytes);
    }

    public static ObjectRequestBody objectBody(String contentType, String content) {
      return new ObjectRequestBody(contentType, content);
    }

    public static ObjectRequestBody objectBody(String contentType, byte[] bytes) {
      return new ObjectRequestBody(contentType, bytes);
    }

    public static ObjectRequestBody objectBody(String contentType, File file) {
      return new ObjectRequestBody(contentType, file);
    }

    public static ObjectRequestBody objectBody(String contentType, InputStream in,
      int contentLength) {
      return new ObjectRequestBody(contentType, in, contentLength);
    }

    public static MultipartFormRequestBody multipartBody() {
      return new MultipartFormRequestBody();
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class FormRequestBody extends HttpRequest.Body {

    private final Multimap params = new Multimap();

    private FormRequestBody() {
      this(null);
    }

    private FormRequestBody(Map<String, Object> params) {
      super("application/x-www-form-urlencoded");
      if (params != null) {
        this.params.addAll(params);
      }
    }

    /**
     * 会替换掉已经存在的 name
     */
    public FormRequestBody setParam(String name, Object value) {
      params.set(name, value);
      return this;
    }

    /**
     * 如果 name 已经存在, 则追加值
     */
    public FormRequestBody addParam(String name, Object value) {
      params.add(name, value);
      return this;
    }

    /**
     * 如果 name 已经存在, 则追加值
     */
    public FormRequestBody addParam(String name, List<Object> values) {
      params.add(name, values);
      return this;
    }

    public FormRequestBody addParams(Map<String, Object> params) {
      this.params.addAll(params);
      return this;
    }

    public Multimap getParams() {
      return params;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class ObjectRequestBody extends HttpRequest.Body {

    private final Object object;
    private int contentLength;

    ObjectRequestBody(String contentType, Object object) {
      super(contentType);
      this.object = object;
    }

    ObjectRequestBody(String contentType, Object object, int contentLength) {
      super(contentType);
      this.object = object;
      this.contentLength = contentLength;
    }

    public Object getObject() {
      return object;
    }

    public int getContentLength() {
      return contentLength;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class MultipartFormRequestBody extends HttpRequest.Body {

    private Map<String, Object> params = new HashMap<>();

    private MultipartFormRequestBody() {
      super("multipart/form-data");
    }

    public MultipartFormRequestBody addFile(String name, File file) {
      params.put(name, file);
      return this;
    }

    public MultipartFormRequestBody addFormData(String name, String value) {
      params.put(name, value);
      return this;
    }

    public MultipartFormRequestBody addFormData(Map<String, Object> params) {
      this.params.putAll(params);
      return this;
    }

    public Map<String, Object> getMultiparts() {
      return params;
    }
  }
}
