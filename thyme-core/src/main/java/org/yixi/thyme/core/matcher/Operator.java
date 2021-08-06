package org.yixi.thyme.core.matcher;

/**
 * @author yixi
 * @since 1.2.0
 */
@SuppressWarnings("all")
public enum Operator {
  eq,

  ne,

  gt,

  gte,

  lt,

  lte,

  in,

  nin,

  anyOf, // 针对数组元素, 至少包含一个元素

  allOf, // 针对数组元素, 全部元素全部包含

  notOf, // 针对数组元素, 元素不包含

  contains, // 字符元素, 包含字符串

  regex, // 字符元素, 正则匹配

  startsWith, // 字符元素, 匹配元素开始

  endsWith, // 字符元素, 匹配元素结束

  exists,

  antPath, // 字符元素, ant style 风格匹配 path 路径

  __not, // 取反

  __and, // 逻辑匹配符: &&

  __or // 逻辑匹配符: ||

}
