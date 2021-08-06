package org.yixi.thyme.core.ex;

/**
 * 没有权限访问资源报此异常
 *
 * @author yixi
 * @since 1.0.0
 */
public class ForbiddenException extends BusinessException {

  private static final long serialVersionUID = 921168844217885564L;

  public ForbiddenException() {
    super(403, "没有权限");
  }

  public ForbiddenException(String message) {
    super(403, message);
  }

  @Override
  public String toString() {
    return "UnauthorizedException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
