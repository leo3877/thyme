package org.yixi.data.client.config;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface Properties {

  String getString(String key);

  Integer getInteger(String key);

  Long getLong(String key);
}
