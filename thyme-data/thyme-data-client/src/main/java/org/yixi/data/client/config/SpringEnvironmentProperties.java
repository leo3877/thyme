package org.yixi.data.client.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * @author yixi
 * @since 1.0.0
 */
public class SpringEnvironmentProperties implements Properties {

  private final Environment environment;

  public SpringEnvironmentProperties(Environment environment) {
    this.environment = environment;
  }

  @Override
  public String getString(String key) {
    return environment.getProperty(key);
  }

  @Override
  public Integer getInteger(String key) {
    String property = environment.getProperty(key);
    if (StringUtils.isNotBlank(property)) {
      return Integer.parseInt(property);
    }
    return null;
  }

  @Override
  public Long getLong(String key) {
    String property = environment.getProperty(key);
    if (StringUtils.isNotBlank(property)) {
      return Long.parseLong(property);
    }
    return null;
  }
}
