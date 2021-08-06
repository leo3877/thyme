/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yixi.thyme.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;

/**
 * 从 Spring 框架中移植而来,增加了部分方法和实现, 删除了大部分注释
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @author Sam Brannen
 * @author Chris Beams
 * @author yixi
 * @since 1.2.2
 */
@SuppressWarnings("all") //  针对外部代码忽略所有 checkstyle 问题
public abstract class ReflectionUtils {

  /**
   * @see #isCglibRenamedMethod
   */
  private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

  private static final Method[] NO_METHODS = {};

  private static final Field[] NO_FIELDS = {};

  private static final ConcurrentMap<Class<?>, Method[]> declaredMethodsCache =
    new ConcurrentHashMap<>(256);

  private static final ConcurrentMap<Class<?>, Field[]> declaredFieldsCache =
    new ConcurrentHashMap<Class<?>, Field[]>(256);

  /**
   * 查找给定名字字段，包括所有父类
   *
   * @param clazz 目标 Class
   * @param name 字段名字
   * @return 如果没有找到返回 null
   */
  public static Field findField(Class<?> clazz, String name) {
    return findField(clazz, name, null);
  }

  /**
   * 查找给定名字、类型字段，包括所有父类
   *
   * @param clazz 目标 Class
   * @param name 字段名字，可以为 null，如果指定了 type 参数
   * @param type 字段类型，可以为 null，如果指定了 name 参数
   * @return 如果没有找到返回 null
   */
  public static Field findField(Class<?> clazz, String name, Class<?> type) {
    Objects.requireNonNull(clazz, "Class must be specified.");
    Assertions.isTrue("Either name or type of the field must be specified",
      name != null || type != null);
    Class<?> searchType = clazz;
    while (Object.class != searchType && searchType != null) {
      Field[] fields = getDeclaredFields(searchType);
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) &&
          (type == null || type.equals(field.getType()))) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * 给字段设值
   */
  public static void setField(Field field, Object target, Object value) {
    try {
      field.set(target, value);
    } catch (IllegalAccessException ex) {
      handleReflectionException(ex);
      throw new IllegalStateException(
        String.format("Unexpected reflection exception - {}: {}",
          ex.getClass().getName(), ex.getMessage()));
    }
  }

  /**
   * 获得字段值
   */
  public static Object getField(Field field, Object target) {
    try {
      if (field == null) {
        return null;
      }
      return field.get(target);
    } catch (IllegalAccessException ex) {
      handleReflectionException(ex);
      throw new IllegalStateException(
        String.format("Unexpected reflection exception - {}: {}",
          ex.getClass().getName(), ex.getMessage()));
    }
  }

  /**
   * 查找给定名字字段，包括所有父类
   *
   * @param clazz 目标 Class
   * @param name 方法名字
   * @return 如果没有找到，返回 null
   */
  public static Method findMethod(Class<?> clazz, String name) {
    return findMethod(clazz, name, new Class<?>[0]);
  }

  /**
   * 查找给定名字、参数列表字段，包括所有父类
   *
   * @param clazz 目标 Class
   * @param name 方法名字
   * @param paramTypes 参数列表，可以为空
   * @return 如果没有找到，返回 null
   */
  public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
    Objects.requireNonNull(clazz, "Class must be specified.");
    Objects.requireNonNull(name, "Method name must be specified.");
    Class<?> searchType = clazz;
    while (searchType != null) {
      Method[] methods =
        searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType);
      for (Method method : methods) {
        if (name.equals(method.getName())
          && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
          return method;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * 调用方法
   */
  public static Object invokeMethod(Method method, Object target) {
    return invokeMethod(method, target, new Object[0]);
  }

  /**
   * 调用方法
   */
  public static Object invokeMethod(Method method, Object target, Object... args) {
    try {
      return method.invoke(target, args);
    } catch (Exception ex) {
      handleReflectionException(ex);
    }
    throw new IllegalStateException("Should never get here");
  }

  public static void handleReflectionException(Exception ex) {
    if (ex instanceof NoSuchMethodException) {
      throw new IllegalStateException("Method not found: " + ex.getMessage());
    }
    if (ex instanceof IllegalAccessException) {
      throw new IllegalStateException("Could not access method: " + ex.getMessage());
    }
    if (ex instanceof InvocationTargetException) {
      handleInvocationTargetException((InvocationTargetException) ex);
    }
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  public static void handleInvocationTargetException(InvocationTargetException ex) {
    rethrowRuntimeException(ex.getTargetException());
  }

  public static void rethrowRuntimeException(Throwable ex) {
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  public static void rethrowException(Throwable ex) throws Exception {
    if (ex instanceof Exception) {
      throw (Exception) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  public static boolean declaresException(Method method, Class<?> exceptionType) {
    Assertions.notNull("Method", method);
    Class<?>[] declaredExceptions = method.getExceptionTypes();
    for (Class<?> declaredException : declaredExceptions) {
      if (declaredException.isAssignableFrom(exceptionType)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPublicStaticFinal(Field field) {
    int modifiers = field.getModifiers();
    return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(
      modifiers));
  }

  public static boolean isEqualsMethod(Method method) {
    if (method == null || !method.getName().equals("equals")) {
      return false;
    }
    Class<?>[] paramTypes = method.getParameterTypes();
    return (paramTypes.length == 1 && paramTypes[0] == Object.class);
  }

  public static boolean isHashCodeMethod(Method method) {
    return (method != null
      && method.getName().equals("hashCode")
      && method.getParameterTypes().length == 0);
  }

  public static boolean isToStringMethod(Method method) {
    return (method != null && method.getName().equals("toString") && method.getParameterTypes
      ().length == 0);
  }

  public static boolean isObjectMethod(Method method) {
    if (method == null) {
      return false;
    }
    try {
      Object.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static boolean isCglibRenamedMethod(Method renamedMethod) {
    String name = renamedMethod.getName();
    if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
      int i = name.length() - 1;
      while (i >= 0 && Character.isDigit(name.charAt(i))) {
        i--;
      }
      return ((i > CGLIB_RENAMED_METHOD_PREFIX.length()) &&
        (i < name.length() - 1) && name.charAt(i) == '$');
    }
    return false;
  }

  public static void makeAccessible(Field field) {
    if ((!Modifier.isPublic(field.getModifiers()) ||
      !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
      Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
      field.setAccessible(true);
    }
  }

  public static void makeAccessible(Method method) {
    if ((!Modifier.isPublic(method.getModifiers()) ||
      !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method
      .isAccessible()) {
      method.setAccessible(true);
    }
  }

  public static void makeAccessible(Constructor<?> ctor) {
    if ((!Modifier.isPublic(ctor.getModifiers()) ||
      !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible
      ()) {
      ctor.setAccessible(true);
    }
  }

  /**
   * 迭代自身类的方法、java8 接口默认方法
   *
   * @see #doWithMethods
   * @since 4.2
   */
  public static void doWithLocalMethods(Class<?> clazz, MethodCallback mc) {
    Method[] methods = getDeclaredMethods(clazz);
    for (Method method : methods) {
      try {
        mc.doWith(method);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException("Not allowed to access method '" + method.getName
          () + "': " + ex);
      }
    }
  }

  /**
   * 迭代所有方、包括父类、java8 接口默认方法
   *
   * @see #doWithMethods
   * @since 4.2
   */
  public static void doWithMethods(Class<?> clazz, MethodCallback mc) {
    doWithMethods(clazz, mc, null);
  }

  public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
    Method[] methods = getDeclaredMethods(clazz);
    for (Method method : methods) {
      if (mf != null && !mf.matches(method)) {
        continue;
      }
      try {
        mc.doWith(method);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException("Not allowed to access method '" + method.getName
          () + "': " + ex);
      }
    }
    if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
      doWithMethods(clazz.getSuperclass(), mc, mf);
    } else if (clazz.isInterface()) {
      for (Class<?> superIfc : clazz.getInterfaces()) {
        doWithMethods(superIfc, mc, mf);
      }
    }
  }

  /**
   * 返回所有叶子类及父类的声明方法，不包括 Object 类,  当 unique 为 true 的时候忽略父类已经存在的方法
   */
  public static List<Method> getAllDeclaredMethods(Class<?> leafClass) {
    final List<Method> methods = new ArrayList<Method>(32);
    doWithMethods(leafClass, new MethodCallback() {
      @Override
      public void doWith(Method method) {
        methods.add(method);
      }
    });
    return methods;
  }

  /**
   * 返回所有方法包括父类，如果子类已经存在，则忽略父类
   */
  public static List<Method> getUniqueDeclaredMethods(Class<?> leafClass) {
    final List<Method> methods = new ArrayList<Method>(32);
    doWithMethods(leafClass, new MethodCallback() {
      @Override
      public void doWith(Method method) {
        boolean knownSignature = false;
        Method methodBeingOverriddenWithCovariantReturnType = null;
        for (Method existingMethod : methods) {
          if (method.getName().equals(existingMethod.getName()) &&
            Arrays.equals(method.getParameterTypes(), existingMethod
              .getParameterTypes())) {
            if (existingMethod.getReturnType() != method.getReturnType()
              && existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
              methodBeingOverriddenWithCovariantReturnType = existingMethod;
            } else {
              knownSignature = true;
            }
            break;
          }
        }
        if (methodBeingOverriddenWithCovariantReturnType != null) {
          methods.remove(methodBeingOverriddenWithCovariantReturnType);
        }
        if (!knownSignature && !isCglibRenamedMethod(method)) {
          methods.add(method);
        }
      }
    });
    return methods;
  }

  /**
   *
   */
  private static Method[] getDeclaredMethods(Class<?> clazz) {
    Assertions.notNull("Class", clazz);
    Method[] result = declaredMethodsCache.get(clazz);
    if (result == null) {
      Method[] declaredMethods = clazz.getDeclaredMethods();
      List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
      if (defaultMethods != null) {
        result = new Method[declaredMethods.length + defaultMethods.size()];
        System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
        int index = declaredMethods.length;
        for (Method defaultMethod : defaultMethods) {
          result[index] = defaultMethod;
          index++;
        }
      } else {
        result = declaredMethods;
      }
      declaredMethodsCache.putIfAbsent(clazz, (result.length == 0 ? NO_METHODS : result));
    }
    return result;
  }

  private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
    List<Method> result = null;
    for (Class<?> ifc : clazz.getInterfaces()) {
      for (Method ifcMethod : ifc.getMethods()) {
        if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
          if (result == null) {
            result = new LinkedList<Method>();
          }
          result.add(ifcMethod);
        }
      }
    }
    return result;
  }

  /**
   * 迭代自身类的 field，不包括父类
   *
   * @since 4.2
   */
  public static void doWithLocalFields(Class<?> clazz, FieldCallback fc) {
    for (Field field : getDeclaredFields(clazz)) {
      try {
        fc.doWith(field);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException(
          "Not allowed to access field '" + field.getName() + "': " + ex);
      }
    }
  }

  /**
   * 迭代所有字段，包括父类
   */
  public static void doWithFields(Class<?> clazz, FieldCallback fc) {
    doWithFields(clazz, fc, null);
  }

  public static List<Field> getAllDeclaredFields(Class<?> leafClass) {
    return getAllDeclaredFields(leafClass, false);
  }

  /**
   * 返回包括父类所有字段, 当 unique 为 true 的时候忽略父类已经存在的字段
   */
  public static List<Field> getAllDeclaredFields(Class<?> leafClass, boolean unique) {
    final List<Field> fields = new ArrayList<Field>(32);
    doWithFields(leafClass, new FieldCallback() {
      @Override
      public void doWith(Field field) {
        if (unique) {
          boolean exists = false;
          for (Field exist : fields) {
            if (field.getName().equals(exist.getName())
              && field.getType() == exist.getType()) {
              exists = true;
              break;
            }
          }
          if (!exists) {
            fields.add(field);
          }
        } else {
          fields.add(field);
        }
      }
    }, COPYABLE_FIELDS);
    return fields;
  }

  private static Field[] getDeclaredFields(Class<?> clazz) {
    Objects.requireNonNull(clazz, "class must be specified.");
    Field[] result = declaredFieldsCache.get(clazz);
    if (result == null) {
      result = clazz.getDeclaredFields();
      declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
    }
    return result;
  }

  /**
   * 将 src 中的字段 copy 到 desc 中，不包括静态字段和 final 字段.<br> src 必须跟 dest 具有一样的类型或者是它的父类.<br>
   * <strong>并且注意，这是一个浅层 copy</strong>
   */
  public static void shallowCopyFieldState(final Object src, final Object dest) {
    Objects.requireNonNull(src, "src must be specified.");
    Objects.requireNonNull(dest, "dest must be specified.");

    if (!src.getClass().isAssignableFrom(dest.getClass())) {
      throw new IllegalArgumentException(String.format(
        "Destination class [{}] must be same or subclass as source class [{}]",
        dest.getClass().getName(), src.getClass().getName()));
    }
    doWithFields(src.getClass(), new FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException,
        IllegalAccessException {
        makeAccessible(field);
        Object srcValue = field.get(src);
        field.set(dest, srcValue);
      }
    }, COPYABLE_FIELDS);
  }

  /**
   * 将 src 中的相同名字和类型的字段 copy 到 desc 中，不包括静态字段和 final 字段.<br>
   * <strong>并且注意，这是一个浅层 copy</strong>
   */
  public static void copyFields(final Object src, final Object dest) {
    copyFields(src, dest, false);
  }

  /**
   * 将 src 中的相同名字和类型的字段 copy 到 desc 中，不包括静态字段和 final 字段.<br>
   * <strong>支持 map copy</strong>
   * <strong>并且注意，这是一个浅层 copy</strong>
   */
  public static void copyFields(final Object src, final Object dest, boolean ignoreNull) {
    Objects.requireNonNull(src, "src must be specified.");
    Objects.requireNonNull(dest, "dest must be specified.");

    if (src instanceof Map) {
      Map<String, Field> fieldMap = Maps.toHashMap(getAllDeclaredFields(dest.getClass(), true),
        field -> field.getName(), field -> field);
      ((Map) src).forEach((k, v) -> {
        Field field = fieldMap.get(k);
        if (field != null) {
          makeAccessible(field);
          if (field.getType() == v.getClass()
            || v instanceof Integer && field.getType() == int.class
            || v instanceof Long && field.getType() == long.class
            || v instanceof Double && field.getType() == double.class
            || v instanceof Float && field.getType() == float.class) {
            setField(field, dest, v);
          } else if (v instanceof String && field.getType() == Date.class) {
            setField(field, dest, Jsons.decode(v.toString(), Date.class));
          } else if (field.getType().isEnum()) {
            setField(field, dest,
              Jsons.decode("\"" + v.toString() + "\"", field.getType()));
          } else if (v instanceof Integer && (field.getType() == Long.class
            || field.getType() == long.class
            || field.getType() == Double.class
            || field.getType() == double.class
            || field.getType() == Float.class
            || field.getType() == float.class)) {
            setField(field, dest,
              Jsons.decode("\"" + v.toString() + "\"", field.getType()));
          } else {
            throw new IllegalStateException(
              "Invalid data type. must be "
                + field.getType().getSimpleName() + ", " + k + " = " + v);
          }
        }
      });
      return;
    }

    List<Field> fields = getAllDeclaredFields(dest.getClass());

    doWithFields(src.getClass(), new FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException,
        IllegalAccessException {
        makeAccessible(field);
        Object srcValue = field.get(src);
        for (Field targetField : fields) {
          if (field.getName().equals(targetField.getName())
            && field.getType() == targetField.getType()) {
            makeAccessible(targetField);
            if (srcValue == null && !ignoreNull || srcValue != null) {
              targetField.set(dest, srcValue);
            }
            break;
          }
        }
      }
    }, COPYABLE_FIELDS);
  }

  /**
   * 迭代所有的字段包括父类，不包括 Object 类
   */
  public static void doWithFields(Class<?> clazz, FieldCallback fc, FieldFilter ff) {
    Class<?> targetClass = clazz;
    do {
      Field[] fields = getDeclaredFields(targetClass);
      for (Field field : fields) {
        if (ff != null && !ff.matches(field)) {
          continue;
        }
        try {
          fc.doWith(field);
        } catch (IllegalAccessException ex) {
          throw new IllegalStateException(
            "Not allowed to access field '" + field.getName() + "': " + ex);
        }
      }
      targetClass = targetClass.getSuperclass();
    }
    while (targetClass != null && targetClass != Object.class);
  }

  /**
   * @since thyme 1.0.0
   */
  public static <T> T newInstance(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  /**
   * @since 4.2.4
   */
  public static void clearCache() {
    declaredMethodsCache.clear();
    declaredFieldsCache.clear();
  }

  public interface MethodCallback {

    void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
  }

  public interface MethodFilter {

    boolean matches(Method method);
  }

  public interface FieldCallback {

    void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
  }

  public interface FieldFilter {

    boolean matches(Field field);
  }

  /**
   * 过滤掉 static 字段和 final 字段
   */
  public static final FieldFilter COPYABLE_FIELDS = new FieldFilter() {

    @Override
    public boolean matches(Field field) {
      return !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field
        .getModifiers()));
    }
  };

  /**
   * 过滤掉桥接方法
   */
  public static final MethodFilter NON_BRIDGED_METHODS = new MethodFilter() {

    @Override
    public boolean matches(Method method) {
      return !method.isBridge();
    }
  };

  /**
   * 过滤掉桥接方法和 Object 类的方法, 只返回用户定义的方法
   */
  public static final MethodFilter USER_DECLARED_METHODS = new MethodFilter() {

    @Override
    public boolean matches(Method method) {
      return (!method.isBridge() && method.getDeclaringClass() != Object.class);
    }
  };
}
