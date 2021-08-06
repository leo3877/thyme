package org.yixi.thyme.core.ex;

import java.util.Date;

/**
 * 重试异常
 *
 * @author baitouweng
 */
public class RetryableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Long retryAfter;

  public RetryableException(String message) {
    this(message, null, null);
  }

  public RetryableException(Throwable cause) {
    this(cause.getMessage(), cause, null);
  }

  public RetryableException(String message, Throwable cause) {
    this(message, cause, null);
  }

  public RetryableException(String message, Date retryAfter) {
    this(message, null, retryAfter);
  }

  public RetryableException(String message, Throwable cause, Date retryAfter) {
    super(message, cause);
    this.retryAfter = retryAfter != null ? retryAfter.getTime() : null;
  }

  public Date retryAfter() {
    return retryAfter != null ? new Date(retryAfter) : null;
  }
}
