package org.yixi.thyme.core.ex;

/**
 * 错误类型枚举类。
 *
 * @author yixi
 * @since 1.0.0
 */
public enum ErrorType {

  // 系统级别异常，不应该直接暴露给实际终端用户
  INNER_ERROR(10000, "系统错误"),

  FETCH_ERROR(10002, "Fetch 数据出错"),
  // 错误不需要转义，可以直接暴露给用户
  BIZ_ERROR(20000, "服务器出错"),

  INVALID_FORMAT_ERROR(20001, "数据格式错误"),

  DUPLICATE_KEY_ERROR(20002, "数据重复错误"),

  SERVER_BUSY_ERROR(20003, "系统繁忙, 稍后再试"),

  RATE_LIMIT_ERROR(20004, "请求过于频繁"),

  TIME_OUT_ERROR(20005, "请求超时");

  private int code;
  private String msg;

  ErrorType(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

  public int getCode() {
    return code;
  }

  public static ErrorType valueOf(int code) {
    ErrorType errorType;
    switch (code) {
      case 10002:
        errorType = FETCH_ERROR;
        break;
      case 20000:
        errorType = BIZ_ERROR;
        break;
      case 20001:
        errorType = INVALID_FORMAT_ERROR;
        break;
      case 20002:
        errorType = DUPLICATE_KEY_ERROR;
        break;
      case 20003:
        errorType = SERVER_BUSY_ERROR;
        break;
      case 20004:
        errorType = RATE_LIMIT_ERROR;
        break;
      case 20005:
        errorType = TIME_OUT_ERROR;
        break;
      default:
        errorType = INNER_ERROR;
        break;
    }
    return errorType;
  }
}
