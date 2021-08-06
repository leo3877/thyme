package org.yixi.data.client.config;

/**
 * API Client 配置信息基类，子类可以继承并添加个性化属性。
 *
 * @author tieguanyin
 */
public class ThymeApiClientProperties {

  public static final String COMMON_BASE_URL = "ofa.api.common.base-url";
  public static final String COMMON_CONNECT_TIMEOUT = "ofa.api.common.connect-timeout";
  public static final String COMMON_READ_TIMEOUT = "ofa.api.common.read-timeout";
  public static final String COMMON_WRITE_TIMEOUT = "ofa.api.common.write-timeout";

  private String baseUrl; // API 主地址，不包括接口路径
  private Integer connectTimeout; // HTTP 连接超时时间
  private Integer readTimeout; // HTTP 读数据超时时间
  private Integer writeTimeout; // HTTP 写数据超时时间

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public Integer getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public Integer getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public Integer getWriteTimeout() {
    return writeTimeout;
  }

  public void setWriteTimeout(int writeTimeout) {
    this.writeTimeout = writeTimeout;
  }

  @Override
  public String toString() {
    return "ThymeApiClientProperties{" +
      "baseUrl='" + baseUrl + '\'' +
      ", connectTimeout=" + connectTimeout +
      ", readTimeout=" + readTimeout +
      ", writeTimeout=" + writeTimeout +
      '}';
  }
}
