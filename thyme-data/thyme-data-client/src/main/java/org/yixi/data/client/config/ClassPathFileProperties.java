package org.yixi.data.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
public class ClassPathFileProperties implements Properties {

  private ConcurrentMap<String, String> properties = new ConcurrentHashMap<>();

  public ClassPathFileProperties(String fileName) {
    try (InputStream inputStream = this.getClass()
      .getClassLoader()
      .getResourceAsStream(fileName)) {
      java.util.Properties p = new java.util.Properties();
      p.load(inputStream);
      p.forEach((k, v) -> properties.put(k.toString(), v.toString()));
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public String getString(String key) {
    return properties.get(key);
  }

  @Override
  public Integer getInteger(String key) {
    String property = properties.get(key);
    if (StringUtils.isNotBlank(property)) {
      return Integer.parseInt(property);
    }
    return null;
  }

  @Override
  public Long getLong(String key) {
    String property = properties.get(key);
    if (StringUtils.isNotBlank(property)) {
      return Long.parseLong(property);
    }
    return null;
  }
}
