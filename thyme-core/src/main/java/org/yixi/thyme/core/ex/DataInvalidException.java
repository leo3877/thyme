package org.yixi.thyme.core.ex;

import java.util.Collections;
import java.util.Map;

/**
 * 数据格式校验失败时，你应该抛出此异常。
 *
 * @author yixi
 * @since 1.0.0
 */
public class DataInvalidException extends BusinessException {

  private DataInvalidException() {
  }

  public DataInvalidException(Map<String, String> keys) {
    super(ErrorType.INVALID_FORMAT_ERROR.getCode(), ErrorType.INVALID_FORMAT_ERROR.getMsg());
    setKeys(keys);
  }

  public static DataInvalidException createSingleField(String key, String value) {
    return new DataInvalidException(Collections.singletonMap(key, value));
  }
}
