package org.yixi.thyme.data.mongo.starter;

import com.mongodb.client.MongoClient;
import javax.inject.Named;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yixi.thyme.data.mongo.DefaultMongoDaoImpl;
import org.yixi.thyme.data.mongo.DefaultMongoDatabaseDao;
import org.yixi.thyme.data.mongo.IdempotentRetryMongoDatabaseDao;
import org.yixi.thyme.data.mongo.MongoClients;
import org.yixi.thyme.data.mongo.MongoDao;
import org.yixi.thyme.data.mongo.MongoDatabaseDao;

/**
 * @author yixi
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ThymeMongoProperties.class)
public class ThymeMongoAutoConfiguration {

  private ThymeMongoProperties properties;

  public ThymeMongoAutoConfiguration(ThymeMongoProperties properties) {
    this.properties = properties;
  }

  @Bean
  public MongoClient mongoClient() {
    return MongoClients.create(properties);
  }

  @Bean
  public MongoDao mongoDao(MongoClient mongoClient) {
    return new DefaultMongoDaoImpl(mongoClient);
  }

  @Bean(name = "mongoDatabaseDao")
  public MongoDatabaseDao mongoDatabaseDao(MongoDao mongoDao) {
    return new DefaultMongoDatabaseDao(mongoDao, properties.getDatabase());
  }

  @Bean(name = "mongoDaoIdempotentRetry")
  public MongoDatabaseDao idempotentMongoDatabaseDao(
    @Named("mongoDatabaseDao") MongoDatabaseDao mongoDao) {
    return new IdempotentRetryMongoDatabaseDao(mongoDao);
  }

}
