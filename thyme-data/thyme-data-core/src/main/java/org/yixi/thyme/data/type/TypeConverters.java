package org.yixi.thyme.data.type;

import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.data.graphdata.LocalDataFetcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * @author yixi
 * @since 1.0.1
 */
public class TypeConverters {

  private final TypeConverter mongoIdTypeConverter = new MongoIdTypeConverter();
  private final TypeConverter<MutablePair<Boolean, Date>> dateTypeConverter = new DateTypeConverter();
  private final TypeConverter lastOperatorConverter = new LastOperatorConverter();
  private final TypeConverter<MutablePair<Boolean, Date>> lastOperatorConverterV2 = new LastOperatorConverterV2();

  private final LocalDataFetcher.DataSourceType dataSourceType;

  public TypeConverters(LocalDataFetcher.DataSourceType dataSourceType) {
    this.dataSourceType = dataSourceType;
  }

  public void convert(Map<String, Object> map) {
    if (dataSourceType == LocalDataFetcher.DataSourceType.Mongo) {
      mongoIdTypeConverter.convert(map);
    }
    recursive(map);
  }

  public void recursive(Map<String, Object> map) {
    List<String> removes = new ArrayList<>();
    for (Entry<String, Object> entry : map.entrySet()) {
      String k = entry.getKey();
      Object v = entry.getValue();
      if (v instanceof List) {
        map.put(k, recursive((List) v));
      } else if (v instanceof Map) {
        MutablePair<Boolean, Date> dateMutablePair = dateTypeConverter.convert((Map) v);
        if (handle(removes, k, map, (MutablePair) dateMutablePair)) {
          continue;
        }
        dateMutablePair = lastOperatorConverterV2.convert((Map) v);
        if (handle(removes, k, map, (MutablePair) dateMutablePair)) {
          continue;
        }
        recursive((Map) v);
      }
    }
    if (removes != null) {
      for (String remove : removes) {
        map.remove(remove);
      }
    }
  }

  private boolean handle(List<String> removes, String key, Map map,
    MutablePair<Boolean, Object> mutablePair) {
    if (mutablePair.getLeft()) {
      if (mutablePair.getRight() != null) {
        map.put(key, mutablePair.getRight());
      } else {
        removes.add(key);
      }
      return true;
    } else {
      return false;
    }
  }

  public List<Object> recursive(List<Object> objects) {
    List<Object> newList = new ArrayList<>();
    for (Object object : objects) {
      if (object instanceof Map) {
        recursive((Map<String, Object>) object);
        newList.add(object);
      } else if (object instanceof List) {
        newList.add(recursive((List) object));
      } else {
        newList.add(object);
      }
    }
    return newList;
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class MongoIdTypeConverter implements TypeConverter {

    @Override
    public Object convert(Map data) {
      Object id = data.get("id");
      if (id != null) {
        data.remove("id");
        data.put("_id", id);
      }

      return null;
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class DateTypeConverter implements TypeConverter<MutablePair<Boolean, Date>> {

    @Override
    public MutablePair<Boolean, Date> convert(Map data) {
      String key = "__date";
      String date = (String) data.get(key);
      if (StringUtils.isNotBlank(date)) {
        try {
          return MutablePair.of(true, Thyme.DATE_TIME_FORMAT_GMT_8.parse(date));
        } catch (Exception e) {
          throw new ThymeException("日期格式必须是：" + Thyme.DATE_TIME_FORMAT + ", date: " + date);
        }
      } else if (data.containsKey(key)) {
        return MutablePair.of(true, null);
      } else {
        return MutablePair.of(false, null);
      }
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  public static class LastOperatorConverter implements TypeConverter {

    @Override
    public Object convert(Map data) {
      Object last = data.get("last");
      if (last != null) {
        data.remove("last");
        if (last instanceof String) {
          String v = (String) last;
          if (v.charAt(v.length() - 1) == 'd') {
            int d = Integer.parseInt(v.substring(0, v.length() - 1)) - 1;
            Date end = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
            if (d == 0) {
              data.put("gte", end);
            } else {
              Date start = DateUtils.addDays(end, -d);
              data.put("gte", start);
              data.put("lt", DateUtils.addDays(start, 1));
            }
          } else {
            throw new IllegalArgumentException("syntax error. unknown modifier: " + v);
          }
        } else if (last instanceof Map) {
          for (String op : ((Map<String, Object>) last).keySet()) {
            String val = (String) ((Map<String, Object>) last).get(op);
            data.put(op, toDate(val));
          }
        } else {
          throw new IllegalArgumentException("syntax error. type invalid: " + last);
        }
        return true;
      }
      return null;
    }

    private Date toDate(String v) {
      if (v.charAt(v.length() - 1) == 'd') {
        int d = Integer.parseInt(v.substring(0, v.length() - 1)) - 1;
        return DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), -d);
      } else {
        throw new IllegalArgumentException("syntax error. unsupported modifier: " + v);
      }
    }
  }

  /**
   * @author yixi
   */
  public static class LastOperatorConverterV2 implements TypeConverter {

    @Override
    public Object convert(Map data) {
      String key = "__last_time";
      String v = (String) data.get(key);
      if (StringUtils.isNotBlank(v)) {
        return MutablePair.of(true, toDate(v));
      } else if (data.containsKey(key)) {
        return MutablePair.of(true, null);
      } else {
        return MutablePair.of(false, null);
      }
    }

    private Date toDate(String v) {
      if (v.charAt(v.length() - 1) == 'd') {
        int d = Integer.parseInt(v.substring(0, v.length() - 1));
        if (d > 0) {
          return DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), -d);
        } else {
          return DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        }
      } else {
        throw new IllegalArgumentException("syntax error. unsupported modifier: " + v);
      }
    }
  }
}
