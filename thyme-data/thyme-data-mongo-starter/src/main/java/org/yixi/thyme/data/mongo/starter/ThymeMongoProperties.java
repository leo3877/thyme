package org.yixi.thyme.data.mongo.starter;

import org.yixi.thyme.data.mongo.MongoClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yixi
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = ThymeMongoProperties.PREFIX)
public class ThymeMongoProperties extends MongoClientConfig {

  public static final String PREFIX = "thyme.mongo";

}
