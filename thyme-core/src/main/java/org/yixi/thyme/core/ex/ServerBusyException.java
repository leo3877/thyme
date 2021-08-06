package org.yixi.thyme.core.ex;

/**
 * 系统繁忙异常
 *
 * @author yixi
 * @since 1.0.0
 */
public class ServerBusyException extends BusinessException {

  public ServerBusyException() {
    super(ErrorType.SERVER_BUSY_ERROR.getCode(), ErrorType.SERVER_BUSY_ERROR.getMsg());
  }

  public ServerBusyException(String message) {
    super(ErrorType.SERVER_BUSY_ERROR.getCode(), message);
  }

  @Override
  public String toString() {
    return "ServerBusyException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
