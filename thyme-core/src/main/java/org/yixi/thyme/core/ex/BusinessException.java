package org.yixi.thyme.core.ex;

import java.util.Map;

/**
 * 具有业务逻辑语意的异常，一般来说，此异常可以可以继续对外抛出，前端可以直接暴露给用户
 *
 * @author yixi
 * @since 1.0.0
 */
public class BusinessException extends ThymeException {

  public BusinessException() {
    super(ErrorType.BIZ_ERROR.getCode(), ErrorType.BIZ_ERROR.getMsg());
  }

  public BusinessException(String message) {
    super(ErrorType.BIZ_ERROR.getCode(), message);
  }

  public BusinessException(int code, String message) {
    super(code, message);
  }

  public BusinessException(String message, Throwable cause) {
    super(ErrorType.BIZ_ERROR.getCode(), message, cause);
  }

  public BusinessException(int code, String message, Throwable cause) {
    super(code, message, cause);
  }

  public BusinessException(int code, String message, Map<String, String> keys, Throwable cause) {
    super(code, message, keys, cause);
  }

  public Map<String, String> getKeys() {
    return keys;
  }

  public void setKeys(Map<String, String> keys) {
    this.keys = keys;
  }

  @Override
  public String toString() {
    return "BusinessException{" +
      "keys=" + keys +
      ", code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
