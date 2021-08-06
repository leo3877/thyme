package org.yixi.thyme.data.mysql.starter;

import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author yixi
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ThymeMysqlProperties.class)
public class ThymeMysqlAutoConfiguration {

  private ThymeMysqlProperties properties;

  public ThymeMysqlAutoConfiguration(ThymeMysqlProperties properties) {
    this.properties = properties;
  }

  @Bean(name = "txManager")
  public PlatformTransactionManager txManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public DSLContext dslContext(DataSource dataSource) {
    TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);
    org.jooq.Configuration configuration = new DefaultConfiguration()
      .set(new DataSourceConnectionProvider(proxy))
      .set(SQLDialect.MYSQL);
    return DSL.using(configuration);
  }
}
