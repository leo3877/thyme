package org.yixi.data.client;

/**
 * @author yixi
 * @since 1.0.1
 */
public interface TypeConverter<Source, Target> {

  Target convert(Source item);
}
