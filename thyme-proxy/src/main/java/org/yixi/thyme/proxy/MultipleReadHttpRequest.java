package org.yixi.thyme.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.yixi.thyme.core.ex.ThymeException;

/**
 * 支持重复读取请求信息的包装类。
 *
 * @author yixi
 */
public class MultipleReadHttpRequest extends HttpServletRequestWrapper {

  private final ByteArrayInputStream in;

  public MultipleReadHttpRequest(HttpServletRequest request) {
    super(request);
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[8 * 1024];
      for (int len = request.getInputStream().read(buffer); len > 0;
        len = request.getInputStream().read(buffer)) {
        out.write(buffer, 0, len);
      }
      in = new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  @Override
  public ServletInputStream getInputStream() {
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isReady() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void setReadListener(ReadListener listener) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int read() {
        return in.read();
      }

      @Override
      public synchronized void reset() {
        in.reset();
      }

      @Override
      public synchronized void mark(int readlimit) {
        in.mark(readlimit);
      }
    };
  }
}