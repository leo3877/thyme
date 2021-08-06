package org.yixi.thyme.data.type;

import java.util.Map;

/**
 * @author yixi
 * @since 1.0.1
 */
public interface TypeConverter<T> {

  T convert(Map map);
}
