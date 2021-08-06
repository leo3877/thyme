package org.yixi.data.client.config;

import org.yixi.data.client.retrofit.JacksonConverterFactory;
import org.yixi.thyme.core.json.Jsons.ThymeObjectMapper;
import org.yixi.thyme.core.util.ReflectionUtils;
import org.yixi.thyme.http.OkHttpTraceInterceptor;
import org.yixi.thyme.http.RetryInterceptor;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Thyme Client Retrofit 工厂类。
 *
 * @author tieguanyin
 * @author yixi
 * @since 1.0.0
 */
public abstract class ThymeApiClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(ThymeApiClientFactory.class);

  /**
   * 10s
   */
  private static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
  private static final long DEFAULT_READ_TIMEOUT = 10_000;
  private static final long DEFAULT_WRITE_TIMEOUT = 10_000;

  private static final Converter.Factory DEFAULT_CONVERTER_FACTORY =
    JacksonConverterFactory.create(ThymeObjectMapper.defaultObjectMapper());

  private static final Converter.Factory converterFactory = DEFAULT_CONVERTER_FACTORY;

  public static Retrofit createRetrofit(String configPrefix, Properties properties) {
    ThymeApiClientProperties self = selfApiClientProperties(configPrefix, properties);
    ThymeApiClientProperties common = commonApiClientProperties(properties);
    logger.info("ClientProperties, common: {}, self: {}", common, self);
    String baseUrl = self.getBaseUrl() != null ? self.getBaseUrl() : common.getBaseUrl();
    Objects.requireNonNull(baseUrl,
      "API base url must be config in application.properties. eg: "
        + ThymeApiClientProperties.COMMON_BASE_URL + "=http://api.foo.com/ or "
        + "ofa.api.{service-name}.base-url=http://api.bar.com/");
    return new Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(converterFactory)
      .addConverterFactory(ScalarsConverterFactory.create())
      .client(createOkHttpClient(self, common))
      .build();
  }

  /**
   * 获取 服务 api 配置, merge selfApiClientProperties 及 commonApiClientProperties
   */
  public static ThymeApiClientProperties apiClientProperties(String configPrefix,
    Properties reader) {
    ThymeApiClientProperties self = selfApiClientProperties(configPrefix, reader);
    ThymeApiClientProperties common = commonApiClientProperties(reader);
    ReflectionUtils.copyFields(self, common, true);
    return common;
  }

  /**
   * 获取 服务个性 api 配置
   */
  public static ThymeApiClientProperties selfApiClientProperties(String configPrefix,
    Properties reader) {
    ThymeApiClientProperties properties = new ThymeApiClientProperties();
    properties.setBaseUrl(reader.getString(configPrefix + ".base-url"));
    Integer connectTimeout = reader.getInteger(configPrefix + ".connect-timeout");
    if (connectTimeout != null) {
      properties.setConnectTimeout(connectTimeout);
    }
    Integer readTimeout = reader.getInteger(configPrefix + ".read-timeout");
    if (readTimeout != null) {
      properties.setReadTimeout(readTimeout);
    }
    Integer writeTimeout = reader.getInteger(configPrefix + ".write-timeout");
    if (writeTimeout != null) {
      properties.setWriteTimeout(writeTimeout);
    }
    return properties;
  }

  /**
   * 获取 thyme 通用 api 配置
   */
  public static ThymeApiClientProperties commonApiClientProperties(Properties reader) {
    ThymeApiClientProperties properties = new ThymeApiClientProperties();
    properties.setBaseUrl(reader.getString(ThymeApiClientProperties.COMMON_BASE_URL));
    Integer connectTimeout = reader.getInteger(ThymeApiClientProperties.COMMON_CONNECT_TIMEOUT);
    if (connectTimeout != null) {
      properties.setConnectTimeout(connectTimeout);
    }
    Integer readTimeout = reader.getInteger(ThymeApiClientProperties.COMMON_READ_TIMEOUT);
    if (readTimeout != null) {
      properties.setReadTimeout(readTimeout);
    }
    Integer writeTimeout = reader.getInteger(ThymeApiClientProperties.COMMON_WRITE_TIMEOUT);
    if (writeTimeout != null) {
      properties.setWriteTimeout(writeTimeout);
    }
    return properties;
  }

  public static OkHttpClient createOkHttpClient(String configPrefix, Properties properties) {
    ThymeApiClientProperties self = selfApiClientProperties(configPrefix, properties);
    ThymeApiClientProperties common = commonApiClientProperties(properties);
    return createOkHttpClient(self, common);
  }

  public static OkHttpClient createOkHttpClient(ThymeApiClientProperties self,
    ThymeApiClientProperties common) {
    Long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    if (self.getConnectTimeout() != null && self.getConnectTimeout() > 0) {
      connectTimeout = (long) self.getConnectTimeout();
    } else if (common.getConnectTimeout() != null && common.getConnectTimeout() > 0) {
      connectTimeout = (long) common.getConnectTimeout();
    }
    Long readTimeout = DEFAULT_READ_TIMEOUT;
    if (self.getReadTimeout() != null && self.getReadTimeout() > 0) {
      readTimeout = (long) self.getReadTimeout();
    } else if (common.getReadTimeout() != null && common.getReadTimeout() > 0) {
      readTimeout = (long) common.getReadTimeout();
    }
    Long writeTimeout = DEFAULT_WRITE_TIMEOUT;
    if (self.getWriteTimeout() != null && self.getWriteTimeout() > 0) {
      writeTimeout = (long) self.getWriteTimeout();
    } else if (common.getWriteTimeout() != null && common.getWriteTimeout() > 0) {
      writeTimeout = (long) common.getWriteTimeout();
    }
    OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder()
      .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
      .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
      .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
      .addInterceptor(new RetryInterceptor()); // 超时、或者网络连接异常自动重试
    OkHttpTraceInterceptor traceInterceptor = new OkHttpTraceInterceptor();
    clientBuilder.addNetworkInterceptor(traceInterceptor);
    clientBuilder.addInterceptor(traceInterceptor);
    return clientBuilder.build();
  }
}
