package org.yixi.thyme.core.ex;

/**
 * 超过速度限制
 *
 * @author yixi
 * @since 1.0.0
 */
public class RateLimitException extends BusinessException {

  /**
   * 每秒 tps
   */
  private Integer rate;

  private RateLimitException() {
  }

  public RateLimitException(Integer rate) {
    super(ErrorType.SERVER_BUSY_ERROR.getCode(), ErrorType.SERVER_BUSY_ERROR.getMsg());
    this.rate = rate;
  }

  public Integer getRate() {
    return rate;
  }

  @Override
  public String toString() {
    return "RateLimitException{" +
      "code=" + code +
      ", rate=" + rate +
      ", message='" + message + '\'' +
      '}';
  }
}
