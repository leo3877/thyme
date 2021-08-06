package org.yixi.thyme.core.ex;

/**
 * 页面未找到
 *
 * @author yixi
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {

  public NotFoundException() {
    this("页面未找到");
  }

  public NotFoundException(String message) {
    super(401, message);
  }

  @Override
  public String toString() {
    return "NotFoundException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
