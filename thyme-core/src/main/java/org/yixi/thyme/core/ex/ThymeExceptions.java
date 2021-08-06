package org.yixi.thyme.core.ex;

import java.io.IOException;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.Request;
import org.yixi.thyme.core.json.Jsons;
import retrofit2.Response;

/**
 * @author yixi
 * @since 1.0.0
 */
public abstract class ThymeExceptions {

  public static void rethrow(Throwable ex) {
    throw new ThymeException(ex.getMessage(), ex);
  }

  public static ThymeException ex(Throwable ex) {
    return new ThymeException(ex.getMessage(), ex);
  }

  public static ThymeException ex(String format, Object... params) {
    return new ThymeException(String.format(format, params));
  }

  public static ThymeException ex(Throwable ex, String format, Object... params) {
    return new ThymeException(String.format(format, params), ex);
  }

  public static BusinessException exBiz(String format, Object... params) {
    return new BusinessException(String.format(format, params));
  }

  public static BusinessException exBiz(Throwable ex, String format, Object... params) {
    return new BusinessException(String.format(format, params), ex);
  }

  public static ThymeException fromRetrofitResponse(Request request, Response response) {
    if (response.isSuccessful()) {
      return null;
    }
    try {
      String errorMsg = response.errorBody().string();
      MediaType mediaType = response.errorBody().contentType();
      if (mediaType != null && "json".equals(mediaType.subtype())) {
        throw ThymeExceptions.from(Jsons.decode(errorMsg, Map.class));
      } else if (response.code() == 401) {
        throw new UnauthorizedException(errorMsg);
      } else if (response.code() == 403) {
        throw new ForbiddenException(errorMsg);
      } else if (response.code() == 404) {
        throw new ThymeException("404，你访问的地址不存在: " + request.url());
      } else {
        throw new ThymeException(
          "rawResponse: " + response.toString() + ", errorBody: " + errorMsg);
      }
    } catch (IOException e) {
      throw new ThymeException(
        String.format("%s executing %s %s", e.getMessage(), request.method(),
          request.url()));
    }
  }

  public static ThymeException from(Map<String, Object> errorMap) {
    Object code = errorMap.get("code");
    if (code instanceof Integer) {
      if (ErrorType.INVALID_FORMAT_ERROR.getCode() == ((Integer) code).intValue()) {
        return new DataInvalidException((Map<String, String>) errorMap.get("fields"));
      } else if (ErrorType.FETCH_ERROR.getCode() == ((Integer) code).intValue()) {
        return new FetchException();
      } else if (ErrorType.BIZ_ERROR.getCode() == ((Integer) code).intValue()) {
        Object message = errorMap.get(ThymeException.MESSAGE_KEY);
        if (message != null) {
          return new BusinessException(message.toString());
        } else {
          return new BusinessException();
        }
      } else if (ErrorType.DUPLICATE_KEY_ERROR.getCode() == ((Integer) code).intValue()) {
        return new DuplicateKeyException((Map<String, String>) errorMap.get("fields"));
      } else if (ErrorType.SERVER_BUSY_ERROR.getCode() == ((Integer) code).intValue()) {
        Object message = errorMap.get(ThymeException.MESSAGE_KEY);
        if (message != null) {
          return new ServerBusyException(message.toString());
        } else {
          return new ServerBusyException();
        }
      } else if (ErrorType.RATE_LIMIT_ERROR.getCode() == ((Integer) code).intValue()) {
        Object rate = errorMap.get("rate");
        return new RateLimitException((Integer) rate);
      } else if (((Integer) code).intValue() == 401) {
        Object message = errorMap.get(ThymeException.MESSAGE_KEY);
        if (message != null) {
          return new UnauthorizedException(message.toString());
        } else {
          return new UnauthorizedException();
        }
      } else if (((Integer) code).intValue() == 403) {
        Object message = errorMap.get(ThymeException.MESSAGE_KEY);
        if (message != null) {
          return new ForbiddenException(message.toString());
        } else {
          return new ForbiddenException();
        }
      } else {
        Object message = errorMap.get(ThymeException.MESSAGE_KEY);
        if (message != null) {
          return new ThymeException((Integer) code, message.toString());
        } else {
          return new ThymeException((Integer) code, "系统错误");
        }
      }
    } else {
      return new ThymeException(Jsons.encode(errorMap));
    }
  }
}
