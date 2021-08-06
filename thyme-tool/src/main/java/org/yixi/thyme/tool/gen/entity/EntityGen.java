package org.yixi.thyme.tool.gen.entity;

import com.google.common.base.CaseFormat;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface EntityGen {

  /**
   * 根据列名获取 entity 属性名, 首字母小写驼峰返回, 例如：user_id => userId。
   */
  default String fieldName(String columnName) {
    return StringUtils.uncapitalize(lowerToCamel(columnName));
  }

  /**
   * 根据表名获取 entity 类名, 忽略前缀: t_
   */
  default String className(String tableName) {
    String nameWithoutPrefix = tableName.replaceFirst("t_", StringUtils.EMPTY);
    return lowerToCamel(nameWithoutPrefix);
  }

  /**
   * 下划线转驼峰。
   */
  default String lowerToCamel(String content) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, content);
  }

  /**
   * 驼峰转小写，下划线分割
   */
  default String camelToLower(String content) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, content);
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  enum Profile {
    Vo,

    JsonVo,

    MysqlDomain,

    MongoDomain;

    public boolean isDomain() {
      return this == MongoDomain || this == MysqlDomain;
    }
  }

  /**
   * @author
   * @since 1.0.1
   */
  class EntitySchema {

    private final String className;
    private final Map<String, JsonStringEntityGen.ClassField> fields = new HashMap<>();

    public EntitySchema(String className) {
      this.className = className;
    }

    public String getClassName() {
      return className;
    }

    public EntitySchema addFiled(JsonStringEntityGen.ClassField classField) {
      fields.put(classField.getFieldName(), classField);
      return this;
    }

    public Map<String, JsonStringEntityGen.ClassField> getFields() {
      return fields;
    }
  }

  /**
   * @author yixi
   * @since 1.0.1
   */
  @Data
  @Builder
  class ClassField {

    private String columnName; // json 属性名字, eg: user_name
    private String fieldName; // 领域模型属性名, eg: userName
    private String comments;
    private String type; // 领域模型字段类型
    private int maxSize;
    private boolean nullable = true;
    private boolean unsigned;
  }
}
