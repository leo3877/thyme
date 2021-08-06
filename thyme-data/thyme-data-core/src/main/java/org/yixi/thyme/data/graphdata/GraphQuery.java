package org.yixi.thyme.data.graphdata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.StringUtils;
import org.yixi.data.client.Fetch;
import org.yixi.thyme.core.BaseEntity;
import org.yixi.thyme.core.Batcher;
import org.yixi.thyme.core.Document;
import org.yixi.thyme.core.ex.FetchException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.core.util.ReflectionUtils;
import org.yixi.thyme.data.Constants;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class GraphQuery {

  private final ConcurrentMap<String, DataFetcher> dataFetchers = new ConcurrentHashMap<>();
  private static final GraphQueryThreadPoolExecutor executor = new GraphQueryThreadPoolExecutor();

  public void registerFetcher(String filed, DataFetcher dataFetcher) {
    dataFetchers.put(filed, dataFetcher);
  }

  public void fetch(List<Object> objects, Map<String, Fetch> fetchs) {
    fetch((Object) objects, fetchs);
  }

  public void fetch(Object object, Map<String, Fetch> fetchs) {
    for (String fetchFiled : fetchs.keySet()) {
      DataFetcher dataFetcher = dataFetchers.get(fetchFiled);
      if (dataFetcher == null) {
        throw new FetchException("未注册 DataFetcher, field: " + fetchFiled);
      }

      final Fetch fetch;
      if (fetchs.get(fetchFiled) != null) {
        fetch = fetchs.get(fetchFiled);
      } else {
        fetch = new Fetch();
      }

      final String field;
      int i = fetchFiled.lastIndexOf(".");
      if (i > 0) {
        field = fetchFiled.substring(i + 1);
      } else {
        field = fetchFiled;
      }
      String fieldName = buildField(dataFetcher, field + "Id");
      List<Object> objects = new ArrayList<>();
      recursive(fetchFiled, object, (f, target) -> objects.add(target));
      if (objects.isEmpty()) {
        break;
      }
      if (dataFetcher instanceof PointerDataFetcher) {
        if (objects.size() > Constants.DEFAULT_MAX_LIMIT) {
          throw new FetchException(
            "Exceeds limit, max limit is "
              + Constants.DEFAULT_MAX_LIMIT
              + ", but your objects is " + objects);
        }
        List<Object> values = new ArrayList<>();
        Map<Object, Object> tmp = new HashMap<>();
        for (Object obj : objects) {
          Object fieldValue = getFieldValue(fieldName, obj);
          if (fieldValue == null) {
            continue;
          }
          Object exists = tmp.get(fieldValue);
          if (exists != null) {
            if (exists instanceof List) {
              ((List) exists).add(obj);
            } else {
              List<Object> list = new ArrayList<>();
              list.add(exists);
              list.add(obj);
              tmp.put(fieldValue, list);
            }
          } else {
            tmp.put(fieldValue, obj);
            values.add(fieldValue);
          }
        }
        if (values.size() > 1) {
          fetch.getFilter().put("id", new Document<>("$in", values));
          List results = ((PointerDataFetcher) dataFetcher).fetch(fetch);
          for (Object obj : results) {
            Object target = tmp.get(getFieldValue("id", obj));
            if (target instanceof List) {
              for (Object o : (List) target) {
                setValue(o, field, obj);
              }
            } else {
              setValue(target, field, obj);
            }
          }
        } else if (values.size() > 0) {
          fetch.getFilter().put("id", values.get(0));
          List results = ((PointerDataFetcher) dataFetcher).fetch(fetch);
          if (results.size() > 0) {
            Object target = tmp.get(getFieldValue("id", results.get(0)));
            if (target instanceof List) {
              for (Object obj : (List) target) {
                setValue(obj, field, results.get(0));
              }
            } else {
              setValue(target, field, results.get(0));
            }
          }
        }
      } else {
        if (dataFetcher instanceof ArrayPointerDataFetcher) {
          Set<Object> ids = new HashSet<>();
          for (Object target : objects) {
            Object fieldValue = getFieldValue(fieldName, target);
            if (fieldValue instanceof List) {
              ids.addAll((List) fieldValue);
            } else if (fieldValue != null) {
              throw new ThymeException("TypeError,filed:" + fieldName + ",must be List");
            }
          }
          new Batcher<Object, Object>(2000) {
            @Override
            public List<Object> run(List<Object> buffer) {
              fetch.getFilter()
                .put("id", new Document<>("$in", buffer));
              return ((ArrayPointerDataFetcher) dataFetcher).fetch(fetch);
            }
          }.addObjects(ids)
            .addChildBatcher(new Batcher<BaseEntity, Void>(100000) {
              @Override
              public List run(List<BaseEntity> buffer) {
                Map<Object, Object> objectMap = new HashMap<>();
                buffer.forEach(v -> objectMap.put(v.getId(), v));
                for (Object target : objects) {
                  Object fieldValue = getFieldValue(fieldName, target);
                  if (fieldValue instanceof List
                    && !((List) fieldValue).isEmpty()) {
                    List value = (List) getFieldValue(field, target);
                    if (value == null) {
                      setValue(target, field, value = new ArrayList<>());
                    }
                    for (Object id : (List) fieldValue) {
                      Object exists = objectMap.get(id);
                      if (exists == null) {
                        Map<Object, Object> map = new HashMap<>();
                        map.put("id", id);
                        exists = map;
                      }
                      value.add(exists);
                    }
                  }
                }
                return null;
              }
            }).flush();
        } else if (dataFetcher instanceof RelationDataFetcher) {
          CountDownLatch countDownLatch = new CountDownLatch(objects.size());
          for (Object target : objects) {
            executor.execute(() -> {
              try {
                Object id = getFieldValue("id", target);
                if (id != null) {
                  fetch.getFilter().put(buildField(dataFetcher,
                    StringUtils.uncapitalize(target.getClass().getSimpleName()) + "Id"), id);
                  setValue(target, field, ((RelationDataFetcher) dataFetcher).fetch(fetch));
                }
              } finally {
                countDownLatch.countDown();
              }
            });
          }
          try {
            countDownLatch.await();
          } catch (InterruptedException e) {
            // ignore
          }
        } else {
          throw new FetchException("[no dataFetcher]," + Jsons.encodePretty(fetchs));
        }
      }
    }
  }

  private String buildField(DataFetcher dataFetcher, String defaultName) {
    String valueField = null;
    if (dataFetcher instanceof LocalDataFetcher) {
      valueField = ((LocalDataFetcher) dataFetcher).getOptions().getField();
    } else if (dataFetcher instanceof RemoteDataFetcher) {
      valueField = ((RemoteDataFetcher) dataFetcher).getOptions().getField();
    }
    if (StringUtils.isBlank(valueField)) {
      valueField = defaultName;
    }
    return valueField;
  }

  private void setValue(Object target, String fieldName, Object value) {
    if (target instanceof Map) {
      ((Map) target).put(fieldName, value);
    } else if (target != null) {
      ReflectionUtils.setField(findField(fieldName, target.getClass()), target, value);
    }
  }

  public void recursive(String fetchField, Object target, DataFetcherFn dataFetcherFn) {
    if (target instanceof List) {
      for (Object obj : (List) target) {
        recursive(fetchField, obj, dataFetcherFn);
      }
      return;
    }
    int index = fetchField.indexOf(".");
    if (index > 0) {
      Object val = getFieldValue(fetchField.substring(0, index), target);
      if (val != null) {
        recursive(fetchField.substring(index + 1), val, dataFetcherFn);
      }
    } else {
      dataFetcherFn.fetch(fetchField, target);
    }
  }

  public static Object getFieldValue(String fieldName, Object target) {
    if (target instanceof Map) {
      return ((Map) target).get(fieldName);
    } else {
      return ReflectionUtils.getField(
        findField(fieldName, target.getClass()), target);
    }
  }

  public static Field findField(String fieldName, Class clazz) {
    Field field = ReflectionUtils.findField(clazz, fieldName);
    if (field == null) {
      throw new ThymeException(
        String.format("%s 不存在 %s 字段", clazz.getSimpleName(), fieldName));
    }
    ReflectionUtils.makeAccessible(field);
    return field;
  }

  interface DataFetcherFn {

    void fetch(String field, Object target);
  }
}
