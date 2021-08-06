package org.yixi.thyme.core.ex;

import java.util.Map;

/**
 * 带 code 和 message 的异常，如果没有更具体的异常，你可以在项目中抛出此异常，这样非常方便异常跨服务传播， 也方便异常统一处理，例如在 REST 接口中，可以方便统一拦截。
 *
 * @author yixi
 * @since 1.0.0
 */
public class ThymeException extends ThymeRuntimeException {

  public static final String CODE_KEY = "code";
  public static final String MESSAGE_KEY = "message";

  protected int code;
  protected String message;
  /**
   * 保存一些返回的 k-v 值
   */
  protected Map<String, String> keys;

  public ThymeException() {
  }

  public ThymeException(String message) {
    this(ErrorType.INNER_ERROR.getCode(), message);
  }

  public ThymeException(String message, Throwable cause) {
    this(ErrorType.INNER_ERROR.getCode(), message, cause);
  }

  public ThymeException(int code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }

  public ThymeException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }

  public ThymeException(int code, String message, Map<String, String> keys, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
    this.keys = keys;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Map<String, String> getKeys() {
    return keys;
  }

  public void setKeys(Map<String, String> keys) {
    this.keys = keys;
  }

  @Override
  public String toString() {
    return "ThymeException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
