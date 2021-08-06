package org.yixi.thyme.core;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.math3.util.Pair;
import org.yixi.thyme.core.ex.BusinessException;
import org.yixi.thyme.core.ex.RetryableException;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.ex.ThymeExceptions;
import org.yixi.thyme.core.util.DocumentMapper;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author yixi
 * @since 1.0.0
 */
@Slf4j
public abstract class Thyme {

  /**
   * 默认时间格式
   */
  public static final String DATE_FORMAT = "yyyy-MM-dd";

  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String TIME_FORMAT = "HH:mm:ss";

  public static final TimeZone GMT8_TIME_ZONE = TimeZone.getTimeZone("GMT+8");

  /**
   * 默认时间格式化工具
   */
  public static final FastDateFormat DATE_TIME_FORMAT_GMT_8 = FastDateFormat.getInstance(
    DATE_TIME_FORMAT, GMT8_TIME_ZONE);

  public static final FastDateFormat TIME_FORMAT_GMT_8 = FastDateFormat
    .getInstance(TIME_FORMAT, GMT8_TIME_ZONE);

  public static final FastDateFormat DATE_FORMAT_GMT_8 = FastDateFormat.getInstance(DATE_FORMAT,
    GMT8_TIME_ZONE);


  public static Platform platform() {
    return Platform.get();
  }

  public static String dateFormat(Date date) {
    return DATE_TIME_FORMAT_GMT_8.format(date);
  }

  public static Date dateParse(String str) {
    try {
      return DATE_TIME_FORMAT_GMT_8.parse(str);
    } catch (ParseException e) {
      throw ex(e.getMessage(), e);
    }
  }

  /**
   * 以纳秒返回 fun 函数的执行时长
   */
  public static long duration(WrapperFn fun) {
    Instant start = Instant.now();
    fun.run();
    return Duration.between(start, Instant.now()).toNanos();
  }

  /**
   * supplier 抛出的 RetryableException 异常会进行重试
   */
  public static <T> T retry(Supplier<T> supplier) {
    return retry(3, supplier);
  }

  /**
   * supplier 抛出的 RetryableException 异常会进行重试
   */
  public static <T> T retry(Retryer retryer, Supplier<T> supplier) {
    return retry(3, supplier);
  }

  public static <T> T retry(int maxAttempts, Supplier<T> supplier) {
    return new Retryer.Default(maxAttempts).retry(supplier);
  }

  public static <T> T retry(long period, long maxPeriod, int maxAttempts, Supplier<T> supplier) {
    return new Retryer.Default(period, maxPeriod, maxAttempts).retry(supplier);
  }


  public static <T> T retryForever(Retryer retryer, Supplier<T> supplier) {
    Retryer copy = retryer.copy();
    while (true) {
      try {
        return copy.retry(supplier);
      } catch (RetryableException ex) {
        copy = retryer.copy();
        log.error("RetryForever. {}", ex.getMessage());
      }
    }
  }

  public static <T> T restResponse(Call<T> call) {
    try {
      Response<T> response = call.execute();
      if (response.isSuccessful()) {
        T body = response.body();
        if (body instanceof BaseEntity && ((BaseEntity) body).getId() == null) {
          return null;
        } else {
          return body;
        }
      } else {
        throw ThymeExceptions.fromRetrofitResponse(call.request(), response);
      }
    } catch (IOException e) {
      Request request = call.request();
      throw new ThymeException(String.format("%s executing %s %s", e.getMessage(), request.method(),
        request.url()));
    }
  }

  public static <K, V> Pair<K, V> getValue(String key, Object obj) {
    if (obj instanceof Map) {
      Map map = (Map) obj;
      Object o = map.get(key);
      if (o != null) {
        return new Pair(o, map);
      }
      String[] items = key.split("\\.");
      Object target = obj;
      for (int i = 0; i < items.length; i++) {
        if (target == null) {
          return null;
        }
        if (target instanceof Map) {
          if (i == items.length - 1) {
            return new Pair(((Map) target).get(items[i]), target);
          }
          target = ((Map) target).get(items[i]);
        } else {
          throw Thyme.ex("%s 必须是 Map 类型", target);
        }
      }
      return null;
    } else {
      throw exUnsupported("不支持 type: %s", obj.getClass().getSimpleName());
    }
  }

  public static <T> List<T> newList(int size, Function<Integer, T> func) {
    List list = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      list.add(func.apply(i));
    }
    return list;
  }

  public static Document toDocument(Object target) {
    return DocumentMapper.toDocument(target);
  }

  public static List<Document> toDocuments(List<Object> targets) {
    return DocumentMapper.toDocuments(targets);
  }

  public static <T> T toObject(Map doc, Class<T> clazz) {
    return DocumentMapper.toObject(doc, clazz);
  }

  public static void rethrow(Throwable ex) {
    ThymeExceptions.rethrow(ex);
  }

  public static ThymeException ex(Throwable ex) {
    return ThymeExceptions.ex(ex);
  }

  public static ThymeException ex(String format, Object... params) {
    return ThymeExceptions.ex(String.format(format, params));
  }

  public static ThymeException ex(Throwable ex, String format, Object... params) {
    return ThymeExceptions.ex(String.format(format, params), ex);
  }

  public static BusinessException exBiz(String format, Object... params) {
    return ThymeExceptions.exBiz(String.format(format, params));
  }

  public static BusinessException exBiz(Throwable ex, String format, Object... params) {
    return ThymeExceptions.exBiz(String.format(format, params), ex);
  }

  public static UnsupportedOperationException exUnsupported(String format, Object... params) {
    return new UnsupportedOperationException(String.format(format, params));
  }

  public static <V> V findAnyMatch(List<V> list, Predicate<V> predicate) {
    if (list == null) {
      return null;
    }
    for (V v : list) {
      if (predicate.test(v)) {
        return v;
      }
    }
    return null;
  }

  /**
   * @author yixi
   */
  public interface WrapperFn {

    void run();
  }
}
