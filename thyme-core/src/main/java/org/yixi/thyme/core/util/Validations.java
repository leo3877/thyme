package org.yixi.thyme.core.util;


import org.yixi.thyme.core.ex.DataInvalidException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class Validations {

  private static final Validator validator;

  static {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  public static void validate(Object... beans) {
    validate(beans);
  }

  public static void validate(boolean ignoreNullField, Object... beans) {
    Map<String, String> errors = new HashMap<>();
    for (Object obj : beans) {
      errors.putAll(validateToMessage(obj, ignoreNullField));
    }
    if (!errors.isEmpty()) {
      throw new DataInvalidException(errors);
    }
  }

  public static void validate(Object bean) {
    validate(bean, false);
  }

  public static void validate(Object bean, boolean ignoreNullField) {
    Map<String, String> fields;
    if (ignoreNullField) {
      fields = validateToMessage(bean, ignoreNullField);
    } else {
      fields = validateToMessage(bean);
    }
    if (fields != null && !fields.isEmpty()) {
      throw new DataInvalidException(fields);
    }
  }

  public static Map<String, String> validateToMessage(Object bean) {
    return toMap(validator.validate(bean));
  }

  public static Map<String, String> validateToMessage(Object bean, boolean ignoreNullField) {
    return validateToMessage(bean, ignoreNullField, null);
  }

  public static Map<String, String> validateToMessage(Class beanType, String propertyName,
    Object value) {
    return toMap(validator.validateValue(beanType, propertyName, value));
  }

  public static Map<String, String> validateToMessage(Object bean, Class<?>... groups) {
    return toMap(validator.validate(bean, groups));
  }

  private static Map<String, String> validateToMessage(Object bean, boolean ignoreNullField,
    String prefix) {
    if (!ignoreNullField) {
      return toMap(validator.validate(bean));
    } else {
      Class<?> beanClass = bean.getClass();
      Map<String, String> fields = new LinkedHashMap<>();
      ReflectionUtils.doWithFields(bean.getClass(), f -> {
        if (!f.isAccessible()) {
          f.setAccessible(true);
        }
        Object val = f.get(bean);
        if (val == null) {
          return;
        }
        if (f.getAnnotation(Valid.class) != null) {
          final String pre;
          if (prefix == null) {
            pre = f.getName();
          } else {
            pre = prefix + "." + f.getName();
          }
          validateToMessage(val, ignoreNullField, pre).forEach(
            (k, v) -> fields.put(pre + "." + k, v));
        } else {
          fields.putAll(validateToMessage(beanClass, f.getName(), val));
        }
      }, ff -> {
        if (ff.getAnnotations().length == 0
          || ff.isEnumConstant()
          || Modifier.isStatic(ff.getModifiers())) {
          return false;
        }
        return true;
      });
      return fields;
    }
  }

  private static Map<String, String> toMap(Set<ConstraintViolation<Object>> cs) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (ConstraintViolation<Object> c : cs) {
      fields.put(c.getPropertyPath().toString(), c.getMessage());
    }
    return fields;
  }
}
