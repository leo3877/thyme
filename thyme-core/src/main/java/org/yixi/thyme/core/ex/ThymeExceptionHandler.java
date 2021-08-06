package org.yixi.thyme.core.ex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class ThymeExceptionHandler extends ResponseEntityExceptionHandler {

  protected static final Logger LOGGER = LoggerFactory.getLogger(ThymeExceptionHandler.class);

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(DataInvalidException.class)
  public ThymeException handleException(DataInvalidException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ex;
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnauthorizedException.class)
  public ThymeException handleException(UnauthorizedException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ex;
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ThymeException handleException(ForbiddenException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ex;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ThymeException handleException(NotFoundException ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ex;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(Throwable.class)
  public ThymeException handleException(Throwable ex) {
    LOGGER.error(ex.getMessage(), ex);
    if (ex instanceof ThymeException) {
      return (ThymeException) ex;
    } else if (ex instanceof NullPointerException) {
      return new ThymeException("空指针", ex);
    } else {
      return new ThymeException(ex.getMessage(), ex);
    }
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
    HttpHeaders headers, HttpStatus status, WebRequest request) {
    LOGGER.error(ex.getMessage());
    return super.handleExceptionInternal(ex, new ThymeException(ex.getMessage(), ex), headers,
      status, request);
  }
}
