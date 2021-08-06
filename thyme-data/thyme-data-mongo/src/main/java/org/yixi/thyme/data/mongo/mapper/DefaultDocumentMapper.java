package org.yixi.thyme.data.mongo.mapper;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.yixi.thyme.core.EntityMap;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.util.Assertions;
import org.yixi.thyme.core.util.PoJoUtils;
import org.yixi.thyme.core.util.ReflectionUtils;
import org.yixi.thyme.data.mongo.MongoUpdate;

/**
 * @author yixi
 * @since 1.0.0
 */
public class DefaultDocumentMapper implements DocumentMapper {

  private final Map<Class, Map<String, Method>> getterMethods = new ConcurrentHashMap<>();
  private final Map<Class, Map<String, Method>> setterMethods = new ConcurrentHashMap<>();

  private final Options options;

  public DefaultDocumentMapper() {
    this(OptionsBuilder.builder().ignoreNull().lowerUnderscore().build());
  }

  public DefaultDocumentMapper(Options options) {
    this.options = options;
  }

  @Override
  public Document toDocument(Object obj) {
    Assertions.notNull("obj", obj);
    Document doc = new Document();
    if (Map.class.isAssignableFrom(obj.getClass())) {
      ((Map) obj).forEach((k, v) -> {
        if (!options.isIgnoreNull()) {
          doc.put(convertKey(k), convert(v));
        } else if (v != null) {
          doc.put(convertKey(k), convert(v));
        }
      });
    } else {
      PoJoUtils.findGetterMethods(obj.getClass(), options.isLowerUnderscore())
        .forEach((key, method) -> {
          try {
            Object val = method.invoke(obj);
            if (!options.isIgnoreNull()) {
              doc.put(key, convert(val));
            } else if (val != null) {
              doc.put(key, convert(val));
            }
          } catch (Exception e) {
            throw new ThymeException(e.getMessage(), e);
          }
        });
    }
    return doc;
  }

  private String convertKey(Object key) {
    String key0;
    if (key instanceof String) {
      key0 = (String) key;
    } else {
      key0 = key.toString();
    }
    if (options.isLowerUnderscore()) {
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key0);
    } else {
      return key0;
    }
  }

  @Override
  public List<Document> toDocuments(List<Object> objects) {
    List newList = new ArrayList(objects.size());
    objects.forEach(item -> newList.add(convert(item)));
    return newList;
  }

  @Override
  public <T> T toObject(Document doc, Class<T> clazz) {
    if (doc == null) {
      return null;
    }
    if (Map.class.isAssignableFrom(clazz)) {
      Map map = clazz == Map.class ? new LinkedHashMap() : (Map) newInstance(clazz);
      map.putAll(doc);
      return (T) map;
    }
    T instance = newInstance(clazz, doc.get("type"));
    PoJoUtils.findSetterMethods(instance.getClass(), options.isLowerUnderscore())
      .forEach((key, method) -> {
        Object val = doc.get(key);
        if (val == null) {
          return;
        }
        Class type = method.getParameters()[0].getType();
        Class<?> valClass = val.getClass();
        Object target;
        if (type.isAssignableFrom(valClass)
          || type == int.class && valClass == Integer.class
          || type == Integer.class && valClass == int.class
          || type == long.class && valClass == Long.class
          || type == Long.class && valClass == long.class
          || type == double.class && valClass == Double.class
          || type == Double.class && valClass == double.class
          || type == float.class && valClass == Float.class
          || type == Float.class && valClass == float.class
          || type == boolean.class && valClass == Boolean.class
          || type == Boolean.class && valClass == boolean.class) {
          if (List.class.isAssignableFrom(type)) {
            if (!List.class.isAssignableFrom(val.getClass())) {
              throw new ThymeException("type must be list. type: " + val.getClass().getName());
            }
            target = toObjects((List) val, method);
          } else if (Map.class.isAssignableFrom(type)) {
            if (!Map.class.isAssignableFrom(val.getClass())) {
              throw new ThymeException("type must be map. type: " + val.getClass().getName());
            }
            target = toObject((Map) val, method);
          } else {
            target = val;
          }
        } else if (Enum.class.isAssignableFrom(type)) {
          target = Enum.valueOf(type, val.toString()); // TODO
        } else if (Document.class.isAssignableFrom(valClass)) {
          target = toObject((Document) val, type);
        } else if (val instanceof Number) {
          if (type == Float.class || type == float.class) {
            target = ((Number) val).floatValue();
          } else if (type == Double.class || type == double.class) {
            target = ((Number) val).doubleValue();
          } else if ((type == Long.class || type == long.class) && val instanceof Integer) {
            target = ((Number) val).longValue();
          } else if ((type == Integer.class || type == int.class) && val instanceof Long) {
            target = ((Number) val).intValue();
          } else {
            throw new ThymeException(
              String.format("type mismatch: target type: %s, actual type: %s, key: %s, content: %s",
                type.getName(), val.getClass().getName(), key, val));
          }
        } else {
          throw new IllegalArgumentException(
            String.format("type mismatch: target type: %s, actual type: %s, key: %s, content: %s",
              type.getName(), val.getClass().getName(), key, val));
        }
        try {
          method.invoke(instance, target);
        } catch (Exception e) {
          throw new ThymeException(e.getMessage(), e);
        }
      });
    return instance;
  }

  private List toList(List list) {
    List newList = new ArrayList();
    list.forEach(l -> {
      if (l instanceof List) {
        newList.add(toList(list));
      } else if (l instanceof Map) {
        newList.add(toMap((Document) l, Map.class));
      } else {
        newList.add(l);
      }
    });
    return newList;
  }

  private Map toMap(Document doc, Class clazz) {
    Map map = clazz == Map.class ? new LinkedHashMap() : (Map) newInstance(clazz);
    doc.forEach((k, v) -> {
      Object res;
      if (v instanceof Document) {
        res = toMap((Document) v, LinkedHashMap.class);
      } else if (v instanceof List) {
        res = toList((List) v);
      } else {
        res = v;
      }
      map.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), res);
    });
    return map;
  }

  @Override
  public MongoUpdate toMongoUpdate(Object obj) {
    Document doc = toDocument(obj);
    doc.remove("id");
    return MongoUpdate.create().set(doc);
  }

  private List<Object> toObjects(List list, Method method) {
    List<Object> newList = new ArrayList<>();
    for (Object obj : list) {
      if (obj == null
        || obj instanceof String
        || obj instanceof Number
        || obj instanceof Boolean
        || obj instanceof Character
        || obj instanceof Date
        || obj instanceof BsonTimestamp) {
        newList.add(obj);
      } else if (obj instanceof Document) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        ParameterizedType genericParameterType = (ParameterizedType) genericParameterTypes[0];
        Type type = genericParameterType.getActualTypeArguments()[0];
        Class valueType;
        if (type instanceof Class) {
          valueType = (Class) type;
        } else if (type instanceof ParameterizedType) {
          valueType = (Class) ((ParameterizedType) type).getRawType();
        } else {
          throw new ThymeException("Class type error: " + type);
        }
        if (valueType == Object.class
          || Map.class.isAssignableFrom(valueType) && valueType != EntityMap.class) {
          newList.add(obj);
        } else {
          newList.add(toObject((Document) obj, valueType));
        }
      } else {
        throw new IllegalArgumentException(
          "type invalid:" + method.getName() + " type is: " + obj.getClass().getName());
      }
    }
    return newList;
  }

  private String fieldName(Method method) {
    if (options.isLowerUnderscore()) {
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getName().substring(3));
    } else {
      return StringUtils.uncapitalize(method.getName().substring(3));
    }
  }

  private Object convert(Object obj) {
    if (obj == null
      || obj instanceof String
      || obj instanceof Number
      || obj instanceof Character
      || obj instanceof Date
      || obj instanceof Boolean
      || obj instanceof ObjectId
      || obj instanceof BsonTimestamp) {
      return obj;
    } else if (obj instanceof List) {
      return toDocuments((List<Object>) obj);
    } else if (obj instanceof Object[]) {
      return toDocuments(Lists.newArrayList((Object[]) obj));
    } else if (obj instanceof Map) {
      return toDocument(obj);
    } else if (obj instanceof Enum) {
      return obj.toString();
    } else {
      return toDocument(obj);
    }
  }

  private static <T> T newInstance(Class<T> clazz) {
    return newInstance(clazz, null);
  }

  private static <T> T newInstance(Class<T> clazz, Object type) {
    Class<T> aClass = clazz;
    if (type != null && type instanceof String) {
      Map<String, Class> classes = (Map) ReflectionUtils
        .getField(ReflectionUtils.findField(clazz, "classes"), clazz);
      if (classes != null) {
        aClass = classes.get(type);
        if (aClass == null) {
          throw new ThymeException("Class not exists in classes container, type: " + type);
        }
      }
    }
    try {
      return aClass.newInstance();
    } catch (
      Exception e) {
      throw new IllegalArgumentException("Cannot instantiate " + clazz.getName(), e);
    }

  }

  private Map<Object, Object> toObject(Map<Object, Object> map, Method method) {
    Map<Object, Object> newMap = new LinkedHashMap<>();
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    ParameterizedType genericParameterType;
    Class keyType = null;
    Type type = null;
    if (genericParameterTypes.length > 0 && genericParameterTypes[0] instanceof ParameterizedType) {
      genericParameterType = (ParameterizedType) genericParameterTypes[0];
      keyType = (Class) genericParameterType.getActualTypeArguments()[0];
      type = genericParameterType.getActualTypeArguments()[1];
    }
    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      Object obj = entry.getValue();
      Object key = entry.getKey();
      if (keyType != null && keyType.isEnum()) {
        key = Enum.valueOf(keyType, (String) key);
      }
      if (type instanceof Class && ((Class) type).isEnum()) {
        newMap.put(key, Enum.valueOf((Class) type, (String) obj));
      } else if (obj == null
        || obj instanceof String
        || obj instanceof Number
        || obj instanceof Boolean
        || obj instanceof ObjectId
        || obj instanceof Character
        || obj instanceof Date
        || obj instanceof BsonTimestamp
        || obj instanceof List) {
        newMap.put(key, obj);
      } else if (obj instanceof Document) {
        Class valueType;
        if (type instanceof Class) {
          valueType = (Class) type;
        } else if (type instanceof ParameterizedType) {
          valueType = (Class) ((ParameterizedType) type).getRawType();
        } else {
          throw new ThymeException("Class type error: " + type);
        }
        if (valueType == Object.class || Map.class.isAssignableFrom(valueType)) {
          newMap.put(key, obj);
        } else {
          newMap.put(key, toObject((Document) obj, valueType));
        }
      } else {
        throw new IllegalArgumentException(
          "type invalid:" + method.getName() + " type is:" + obj.getClass().getName());
      }
    }
    return newMap;
  }

}
