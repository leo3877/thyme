package org.yixi.thyme.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author yixi
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
public class RouterConfig {

  private String location;
  private Operator operator;
  private Route route;

  public RouterConfig() {

  }

  /**
   * @author yixi
   */
  @Data
  @Builder
  @AllArgsConstructor
  public static class Route {

    private Type type;
    private String value;

    private String location;

    /**
     * 是否保留匹配路径
     */
    private boolean keep = true;

    public Route() {

    }


  }

  /**
   * @author yixi
   */
  public enum Operator {
    eq,

    contains, // 包含

    regex, // 字符元素, 正则匹配

    startsWith, // 匹配开始

    endsWith, // 匹配结束

    antPath // 字符元素, ant style 风格匹配 path 路径
  }

  /**
   * @author yixi
   */
  public enum Type {
    Proxy,

    Redirect,

    Script
  }

}

