package org.yixi.thyme.core.ex;

/**
 * Fetch 异常类。
 *
 * @author yixi
 * @since 1.0.0
 */
public class FetchException extends ThymeException {

  private static final long serialVersionUID = 1396733172730061517L;

  public FetchException() {
    super(ErrorType.FETCH_ERROR.getCode(), ErrorType.FETCH_ERROR.getMsg());
  }

  public FetchException(String message) {
    super(ErrorType.FETCH_ERROR.getCode(), message);
  }

  @Override
  public String toString() {
    return "FetchException{" +
      "code=" + code +
      ", message='" + message + '\'' +
      '}';
  }
}
