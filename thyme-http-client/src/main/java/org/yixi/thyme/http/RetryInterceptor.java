package org.yixi.thyme.http;

import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.core.ex.TimeoutException;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重试拦截器
 *
 * @author baitouweng
 * @author yixi
 * @since 1.0.0
 */
public class RetryInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    String idempotent = request.header("X-Idempotent");
    if (idempotent != null && "false".equals(idempotent)) {
      return chain.proceed(request);
    }
    return retryProceed(chain);
  }

  private Response retryProceed(Chain chain) throws IOException {
    Request request = chain.request();
    try {
      return Thyme.retry(() -> {
        try {
          return chain.proceed(request);
        } catch (IOException e) {
          throw new RetryableException(
            String.format("%s executing %s %s", e.getMessage(), request.method(), request.url()), e,
            null);
        }
      });
    } catch (RetryableException e) {
      logger.warn(e.getMessage(), e);
      throw new TimeoutException(e.getMessage());
    }
  }
}
