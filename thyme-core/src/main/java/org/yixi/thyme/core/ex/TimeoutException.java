package org.yixi.thyme.core.ex;

/**
 * 请求超时异常
 *
 * @author yixi
 * @since 1.0.0
 */
public class TimeoutException extends ThymeException {


  public TimeoutException() {
    super(ErrorType.TIME_OUT_ERROR.getCode(), ErrorType.TIME_OUT_ERROR.getMsg());
  }

  public TimeoutException(String message) {
    super(ErrorType.TIME_OUT_ERROR.getCode(), message);
  }

  public TimeoutException(String message, Throwable cause) {
    super(ErrorType.TIME_OUT_ERROR.getCode(), message, cause);
  }

  @Override
  public String toString() {
    return "TimeoutException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
