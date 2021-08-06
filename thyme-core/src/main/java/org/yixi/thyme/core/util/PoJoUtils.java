package org.yixi.thyme.core.util;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yixi
 * @since 1.0.0
 */
public class PoJoUtils {

  private final static Map<Class, Map<String, Method>> getterMethods = new ConcurrentHashMap<>();
  private final static Map<Class, Map<String, Method>> setterMethods = new ConcurrentHashMap<>();

  public static Map<String, Method> findGetterMethods(Class clazz, boolean isLowerUnderscore) {
    if (getterMethods.get(clazz) != null) {
      return getterMethods.get(clazz);
    }
    synchronized (PoJoUtils.class) {
      Map<String, Method> methods = new HashMap<>();
      ReflectionUtils.doWithMethods(clazz,
        m -> methods.put(fieldName(m, isLowerUnderscore), m),
        m -> m.getName().startsWith("get") && m.getParameters().length == 0);
      getterMethods.putIfAbsent(clazz, methods);
      return methods;
    }
  }

  public static Map<String, Method> findSetterMethods(Class clazz, boolean isLowerUnderscore) {
    if (setterMethods.get(clazz) != null) {
      return setterMethods.get(clazz);
    }
    synchronized (PoJoUtils.class) {
      Map<String, Method> methods = new HashMap<>();
      ReflectionUtils.doWithMethods(clazz,
        m -> methods.put(fieldName(m, isLowerUnderscore), m),
        m -> m.getName().startsWith("set") && m.getParameters().length == 1);
      setterMethods.putIfAbsent(clazz, methods);
      return methods;
    }
  }

  public static String fieldName(Method method, boolean isLowerUnderscore) {
    if (isLowerUnderscore) {
      return lowerUnderscore(method.getName().substring(3));
    } else {
      return StringUtils.uncapitalize(method.getName().substring(3));
    }
  }

  public static String lowerUnderscore(String name) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
  }
}
