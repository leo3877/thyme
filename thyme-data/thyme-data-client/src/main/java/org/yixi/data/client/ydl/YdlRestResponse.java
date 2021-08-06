package org.yixi.data.client.ydl;

import java.io.Serializable;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.yixi.thyme.core.ex.ErrorType;
import org.yixi.thyme.core.ex.ThymeException;

@Data
@Builder
public class YdlRestResponse<T> implements Serializable {

  private String code;
  private T data;
  private String msg;
  private String errMsg;

  public static <T> YdlRestResponse<T> success(T data) {
    return YdlRestResponse.<T>builder().code("200").msg("成功").build();
  }

  public static <T> YdlRestResponse<T> fail(ThymeException ex) {
    YdlRestResponseBuilder<Map<String, String>> builder = YdlRestResponse
      .<Map<String, String>>builder()
      .code(ex.getCode() + "")
      .errMsg(ex.getMessage())
      .data(ex.getKeys());
    if (ex.getCode() < ErrorType.BIZ_ERROR.getCode()) {
      builder.msg("系统维护中, 稍后再试");
    } else {
      builder.msg(ex.getMessage());
    }
    return (YdlRestResponse) builder.build();
  }

  public static <T> YdlRestResponse<T> fail(T data, String code, String msg) {
    return YdlRestResponse.<T>builder().code(code).msg(msg).data(data).build();
  }
}
