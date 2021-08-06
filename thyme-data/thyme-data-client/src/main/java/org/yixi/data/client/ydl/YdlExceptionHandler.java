package org.yixi.data.client.ydl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.yixi.thyme.core.Thyme;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
@Slf4j
public class YdlExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(Throwable.class)
  public YdlRestResponse handleException(Throwable ex) {
    log.error(ex.getMessage(), ex);
    if (ex instanceof ThymeException) {
      return YdlRestResponse.fail((ThymeException) ex);
    } else if (ex instanceof NullPointerException) {
      return YdlRestResponse.fail(Thyme.ex(ex, "空指针"));
    } else {
      return YdlRestResponse.fail(Thyme.ex(ex, ex.getMessage()));
    }
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
    HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.error(ex.getMessage());
    return super.handleExceptionInternal(ex, new ThymeException(ex.getMessage(), ex), headers,
      status, request);
  }
}
