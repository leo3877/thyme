package org.yixi.thyme.data.mysql.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yixi
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = ThymeMysqlProperties.PREFIX)
public class ThymeMysqlProperties {

  public static final String PREFIX = "thyme.mysql";
}
