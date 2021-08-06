package org.yixi.thyme.data.mysql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import org.jooq.Table;
import org.jooq.TableField;
import org.yixi.thyme.core.ex.BusinessException;
import org.yixi.thyme.core.util.Assertions;
import org.yixi.thyme.core.util.Maps;
import org.yixi.thyme.core.util.ReflectionUtils;

/**
 * @author yixi
 */
public class JooqClassMeta {

  /**
   * domain class 名字
   */
  private final Class clazz;
  /**
   * Simple class Name
   */
  private final String className;
  /**
   * domain 对应的 JOOQ Table
   */
  private final Table table;
  /**
   * domain 的字段
   */
  private final Map<String, Field> fieldMap;
  /**
   * domain 字段对应的 JOOQ 字段
   */
  private final Map<String, TableField> tableFields = new HashMap<>();

  public JooqClassMeta(Class clazz, Table table) {
    Assertions.notNull("clazz", clazz);
    Assertions.notNull("table", table);

    this.clazz = clazz;
    this.table = table;
    this.className = clazz.getSimpleName();

    fieldMap = Maps.toHashMap(ReflectionUtils.getAllDeclaredFields(clazz, true),
      o -> o.getName(), o -> {
        ReflectionUtils.makeAccessible(o);
        return o;
      });

    fieldMap.forEach((k, v) -> {
      org.jooq.Field field = table.field(k);
      if (field == null) {
        Column column = v.getAnnotation(Column.class);
        if (column != null && column.name() != null) {
          field = table.field(column.name());
        }
      }
      if (field != null) {
        tableFields.put(k, (TableField) field);
      }
    });
  }

  public Class getClazz() {
    return clazz;
  }

  public String getClassName() {
    return className;
  }

  public Field getField(String name) {
    return fieldMap.get(name);
  }

  public Map<String, Field> getFieldMap() {
    return fieldMap;
  }

  public TableField getTableField(String name) {
    return tableFields.get(name);
  }

  public Map<String, TableField> getTableFields() {
    return tableFields;
  }

  public Table getTable() {
    return table;
  }

  public Map<String, TableField> filterTableFields(Map<String, Boolean> fields) {
    if (fields == null || fields.isEmpty()) {
      return Maps.newHashMap(tableFields);
    }
    Map<String, TableField> filtered = new HashMap<>();
    String key = fields.keySet().iterator().next();
    Boolean show = fields.get(key);
    if (show) {
      fields.forEach((k, v) -> {
        if (!v) {
          throw new BusinessException("字段语义模糊, fields: " + fields);
        }
        TableField tableField = tableFields.get(k);
        if (tableField != null) {
          filtered.put(k, tableField);
        } else {
          throw new BusinessException("Field not exists, field: " + k);
        }
      });
    } else {
      tableFields.forEach((k, v) -> {
        if (fields.get(k) != null && fields.get(k)) {
          throw new BusinessException("字段语义模糊, fields: " + fields);
        }
        if (fields.get(k) == null) {
          filtered.put(k, v);
        }
      });
    }
    filtered.putIfAbsent("id", tableFields.get("id")); // id 为必须字段
    return filtered;
  }
}
