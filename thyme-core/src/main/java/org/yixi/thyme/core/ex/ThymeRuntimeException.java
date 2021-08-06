package org.yixi.thyme.core.ex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Thyme 运行时异常抽象基类, 自定义运行时异常都应该继承此基类
 *
 * @author yixi
 * @since 1.0.0
 */
@JsonIgnoreProperties({"stackTrace", "stack_trace", "localizedMessage", "localized_message",
  "suppressed", "cause", "rootCause", "root_cause"})
public abstract class ThymeRuntimeException extends RuntimeException {

  public ThymeRuntimeException() {
    super();
  }

  public ThymeRuntimeException(String message) {
    super(message);
  }

  public ThymeRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    return Helper.buildMessage(super.getMessage(), getCause());
  }

  public Throwable getRootCause() {
    return Helper.getRootCause(this);
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class Helper {

    public static String buildMessage(String message, Throwable cause) {
      if (cause == null) {
        return message;
      }
      StringBuilder sb = new StringBuilder(64);
      if (message != null) {
        sb.append(message).append("; ");
      }
      sb.append("thyme exception is ").append(cause);
      return sb.toString();
    }

    public static Throwable getRootCause(Throwable original) {
      if (original == null) {
        return null;
      }
      Throwable rootCause = null;
      Throwable cause = original.getCause();
      while (cause != null && cause != rootCause) {
        rootCause = cause;
        cause = cause.getCause();
      }
      return rootCause;
    }
  }
}
