package org.yixi.thyme.data.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.yixi.data.client.QueryResponse;
import org.yixi.data.client.YixiQuery;
import org.yixi.thyme.data.graphdata.LocalDataFetcher;
import org.yixi.thyme.data.mongo.biz.MongoBaseDao;
import org.yixi.thyme.data.mongo.parser.Type;
import org.yixi.thyme.data.type.TypeConverters;

/**
 * @author yixi
 * @since 1.0.0
 */
public class QueryHelper {

  private final MongoBaseDao mongoBaseManager;
  private static final TypeConverters converters = new TypeConverters(
    LocalDataFetcher.DataSourceType.Mongo);

  public QueryHelper(MongoBaseDao mongoBaseManager) {
    this.mongoBaseManager = mongoBaseManager;
  }

  public <T> QueryResponse<T> query(YixiQuery yixiQuery) {
    return query(from(yixiQuery));
  }

  public <T> QueryResponse<T> query(MongoQuery mongoQuery) {
    QueryResponse<T> findResponse = new QueryResponse<>();
    if (mongoQuery.isCount()) {
      findResponse.setAmount(mongoBaseManager.count(mongoQuery.filter()));
    }
    if (mongoQuery.limit() != -1) {
      findResponse.setObjects(mongoBaseManager.findMany(mongoQuery));
      findResponse.setLimit(mongoQuery.limit());
      findResponse.setSkip(mongoQuery.skip());
      findResponse.setSize(findResponse.getObjects().size());
    }
    return findResponse;
  }

  public static MongoQuery from(YixiQuery query) {
    return MongoQuery.create(toMongoFilter(query.getFilter()))
      .limit(query.getLimit())
      .skip(query.getSkip())
      .fields(query.getFields())
      .sort(query.getSorts())
      .count(query.isCount());
  }

  public static Map toMongoFilter(Map yixiQueryFilter) {
    YixiQuery.Validator.validate(yixiQueryFilter);
    converters.convert(yixiQueryFilter);
    return parse(yixiQueryFilter);
  }

  public static String toSql(YixiQuery query, Type type) {
    return null;
  }

  static Object val(Object val) {
    if (val instanceof String) {
      return "'" + val + "'";
    } else {
      return val;
    }
  }

  static List<Map> parse(List<Map> list) {
    List newList = new ArrayList();
    list.forEach(item -> {
      Map map = parse(item);
      if (!map.isEmpty()) {
        newList.add(map);
      }
    });
    return newList;
  }

  static Map<String, Object> parse(Map<String, Object> map) {
    Map<String, Object> newMap = new HashMap<>();
    map.forEach((k, v) -> {
      if (k.equals("__and")) {
        List<Object> and = parse((List) v);
        if (!and.isEmpty()) {
          newMap.put("$and", and);
        }
      } else if (k.equals("__or")) {
        List<Object> or = parse((List) v);
        if (!or.isEmpty()) {
          newMap.put("$or", or);
        }
      } else if (v instanceof Map) {
        Map<String, Object> m = (Map<String, Object>) v;
        Map<String, Object> valueMap = new HashMap<>();
        Object not = m.get("__not");
        if (not != null) {
          Map notMap = new HashMap();
          if (not instanceof Map) {
            m = (Map) not;
            notMap.put("$not", valueMap);
          } else {
            m = null;
            notMap.put("$not", not);
          }
          if (not instanceof Map
            && ((Map) not).get("startsWith") == null
            && ((Map) not).get("endsWith") == null
            && ((Map) not).get("contains") == null) {
            newMap.put(k, notMap);
          } else if (!(not instanceof Map)) {
            newMap.put(k, notMap);
          } else {
            newMap.put(k, valueMap);
          }
        } else {
          newMap.put(k, valueMap);
        }
        if (m != null && !m.isEmpty()) {
          m.forEach((k1, v1) -> {
            if (v1 == null
              || v1 instanceof Map && ((Map) v1).isEmpty()
              || v1 instanceof List && ((List) v1).isEmpty()) {
              return;
            }
            if (k1.equals("eq")) {
              valueMap.put("$eq", v1);
            } else if (k1.equals("ne")) {
              valueMap.put("$ne", v1);
            } else if (k1.equals("gt")) {
              valueMap.put("$gt", v1);
            } else if (k1.equals("gte")) {
              valueMap.put("$gte", v1);
            } else if (k1.equals("lt")) {
              valueMap.put("$lt", v1);
            } else if (k1.equals("lte")) {
              valueMap.put("$lte", v1);
            } else if (k1.equals("in")) {
              valueMap.put("$in", v1);
            } else if (k1.equals("nin")) {
              valueMap.put("$nin", v1);
            } else if (k1.equals("exists")) {
              valueMap.put("$exists", v1);
            } else if (k1.equals("regex")) {
              valueMap.put("$regex", escapeRegexString((String) v1));
            } else if (k1.equals("contains")) {
              if (StringUtils.isNotBlank((String) v1)) {
                if (not != null) {
                  valueMap.put("$regex", "^((?!" + escapeRegexString((String) v1) + ").)*$");
                } else {
                  valueMap.put("$regex", escapeRegexString((String) v1));
                }
              }
            } else if (k1.equals("startsWith")) {
              if (StringUtils.isNotBlank((String) v1)) {
                if (not != null) {
                  valueMap.put("$regex", "^(?!" + escapeRegexString((String) v1) + ")");
                } else {
                  valueMap.put("$regex", "^" + escapeRegexString((String) v1));
                }
              }
            } else if (k1.equals("endsWith")) {
              if (StringUtils.isNotBlank((String) v1)) {
                if (not != null) {
                  valueMap.put("$regex", ".*(?<!" + escapeRegexString((String) v1) + ")$");
                } else {
                  valueMap.put("$regex", escapeRegexString((String) v1) + "$");
                }
              }
            } else if (k1.equals("allOf")) {
              valueMap.put("$all", v1);
            } else if (k1.equals("notOf")) {
              Map<String, Object> notOf = new HashMap<>();
              notOf.put("$all", v1);
              valueMap.put("$not", notOf);
            } else if (k1.equals("anyOf")) {
              Map<String, Object> match = new HashMap<>();
              match.put("$in", v1);
              valueMap.put("$elemMatch", match);
            } else if (k1.equals("nearSphere")) {
              Map<String, Object> nearSphere = new HashMap<>();
              Map<String, Object> temp = (Map) v1;
              HashMap<Object, Object> geometry = new HashMap<>();
              geometry.put("type", "Point");
              geometry.put("coordinates", temp.get("coordinates"));
              nearSphere.put("$geometry", geometry);
              if (temp.get("minDistance") != null) {
                nearSphere.put("$minDistance", temp.get("minDistance"));
              }
              if (temp.get("maxDistance") != null) {
                nearSphere.put("$maxDistance", temp.get("maxDistance"));
              }
              valueMap.put("$nearSphere", nearSphere);
            } else {
              throw new UnsupportedOperationException(
                "Syntax invalid, Unsupported Operator: " + k1);
            }
          });
          if (valueMap.isEmpty()) {
            newMap.remove(k);
          }
        } else {
          newMap.remove(k);
        }

      } else {
        newMap.put(k, v);
      }
    });
    return newMap;
  }

  private static String escapeRegexString(String val) {
    if (StringUtils.isNotBlank(val)) {
      String[] keywords = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
      for (String key : keywords) {
        if (val.contains(key)) {
          val = val.replace(key, "\\" + key);
        }
      }
    }
    return val;
  }

}
