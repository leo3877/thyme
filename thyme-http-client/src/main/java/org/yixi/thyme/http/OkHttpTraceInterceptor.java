package org.yixi.thyme.http;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yixi
 * @since 1.0.0
 */
public class OkHttpTraceInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    try {
      // TODO
      Response response = chain.proceed(request);

      return response;
    } catch (Throwable t) {
      throw t;
    } finally {
    }
  }
}
