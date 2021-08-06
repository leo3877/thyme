package org.yixi.thyme.core.ex;

import java.util.Map;

/**
 * 值重复异常，Thyme 也会将数据库唯一键重复异常转义成此异常
 *
 * @author yixi
 * @since 1.0.0
 */
public class DuplicateKeyException extends BusinessException {

  private DuplicateKeyException() {
  }

  public DuplicateKeyException(Map<String, String> keys) {
    super(ErrorType.DUPLICATE_KEY_ERROR.getCode(), ErrorType.DUPLICATE_KEY_ERROR.getMsg());
    setKeys(keys);
  }

}
