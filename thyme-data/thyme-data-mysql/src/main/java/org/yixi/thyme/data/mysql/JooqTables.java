package org.yixi.thyme.data.mysql;

import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.Assertions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jooq.Table;

/**
 * @author yixi
 */
public abstract class JooqTables {

  private static final ConcurrentMap<Class, JooqClassMeta> classMetas = new ConcurrentHashMap<>();

  public static void register(Class clazz, Table table) {
    Assertions.notNull("clazz", clazz);
    Assertions.notNull("table", table);
    classMetas.put(clazz, new JooqClassMeta(clazz, table));
  }

  public static JooqClassMeta classMeta(Class clazz) {
    Assertions.notNull("clazz", clazz);
    return classMetas.get(clazz);
  }

  public static JooqClassMeta classMetaRequire(Class clazz) {
    JooqClassMeta classMeta = classMeta(clazz);
    if (classMeta == null) {
      throw new ThymeException("clazz must register Table. class: " + clazz.getSimpleName());
    }
    return classMeta;
  }
}
