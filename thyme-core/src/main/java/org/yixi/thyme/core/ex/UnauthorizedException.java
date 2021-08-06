package org.yixi.thyme.core.ex;

/**
 * 没有权限访问资源报此异常
 *
 * @author yixi
 * @since 1.0.0
 */
public class UnauthorizedException extends ThymeException {

  private static final long serialVersionUID = 921168844217885564L;

  public UnauthorizedException() {
    super(401, "未授权");
  }

  public UnauthorizedException(String message) {
    super(401, message);
  }

  @Override
  public String toString() {
    return "UnauthorizedException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
