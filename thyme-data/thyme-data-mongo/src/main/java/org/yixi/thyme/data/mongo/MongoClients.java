package org.yixi.thyme.data.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * @author yixi
 * @since 1.0.0
 */
@Slf4j
public class MongoClients {

  public static MongoClient create(MongoClientConfig clientConfig) {
    if (clientConfig.getUrls() == null || clientConfig.getUrls().isEmpty()) {
      throw new ThymeException(
        String.format("thyme.mongo.urls 未配置, thyme.mongo.urls.urls=host1:port,host2:port"));
    }

    log.info("Create MongoClient: {}", clientConfig);

    MongoClientSettings.Builder builder = MongoClientSettings.builder();
    builder.applyConnectionString(
      new ConnectionString("mongodb://" + clientConfig.getUrls()));
    String readPreference = clientConfig.getReadPreference();
    if (readPreference != null) {
      if ("primary".equals(readPreference)) {
        builder.readPreference(com.mongodb.ReadPreference.primary());
      } else if ("primaryPreferred".equals(readPreference)) {
        builder.readPreference(com.mongodb.ReadPreference.primaryPreferred());
      } else if ("secondary".equals(readPreference)) {
        builder.readPreference(com.mongodb.ReadPreference.secondary());
      } else if ("secondaryPreferred".equals(readPreference)) {
        builder.readPreference(com.mongodb.ReadPreference.secondaryPreferred());
      } else if ("nearest".equals(readPreference)) {
        builder.readPreference(com.mongodb.ReadPreference.nearest());
      }
    }
    if (StringUtils.isNotBlank(clientConfig.getUsername())
      && StringUtils.isNotBlank(clientConfig.getPassword())) {
      builder.credential(MongoCredential.createCredential(clientConfig.getUsername(),
        clientConfig.getAuthDatabase(), clientConfig.getPassword().toCharArray()));
    }
    // TODO  support writeConcern
    return com.mongodb.client.MongoClients.create(builder.build());
  }
}
