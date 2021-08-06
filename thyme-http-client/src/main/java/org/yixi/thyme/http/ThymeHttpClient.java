package org.yixi.thyme.http;

import com.google.common.net.MediaType;
import org.yixi.thyme.core.ObjectId;
import org.yixi.thyme.core.ex.ThymeException;
import org.yixi.thyme.core.json.Jsons;
import org.yixi.thyme.http.HttpRequest.Multimap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import okhttp3.internal.Util;
import okio.Buffer;

/**
 * @author yixi
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface ThymeHttpClient {

  String call(HttpRequest request);

  <T> T call(HttpRequest request, Class<T> clazz);

  String get(String url);

  String get(String url, Map<String, Object> headers);

  <T> T get(String url, Class<T> clazz);

  <T> T get(String url, Map<String, Object> headers, Class<T> clazz);

  String postForm(String url);

  String postForm(String url, Map<String, Object> params);

  String postForm(String url, Map<String, Object> headers, Map<String, Object> params);

  <T> T postForm(String url, Class<T> clazz);

  <T> T postForm(String url, Map<String, Object> params, Class<T> clazz);

  <T> T postForm(String url, Map<String, Object> headers, Map<String, Object> params,
    Class<T> clazz);

  String postJson(String url, Object obj);

  String postJson(String url, Map<String, Object> headers, Object obj);

  <T> T postJson(String url, Object obj, Class<T> clazz);

  <T> T postJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz);

  String putJson(String url, Object obj);

  String putJson(String url, Map<String, Object> headers, Object obj);

  <T> T putJson(String url, Object obj, Class<T> clazz);

  <T> T putJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz);

  String patchJson(String url, Object obj);

  String patchJson(String url, Map<String, Object> headers, Object obj);

  <T> T patchJson(String url, Object obj, Class<T> clazz);

  <T> T patchJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz);

  String deleteJson(String url, Object obj);

  String deleteJson(String url, Map<String, Object> headers, Object obj);

  <T> T deleteJson(String url, Object obj, Class<T> clazz);

  <T> T deleteJson(String url, Map<String, Object> headers, Object obj, Class<T> clazz);

  static <T> T read(HttpRequest request, InputStream stream, Long contentLength,
    MediaType mediaType, Class<T> clazz) {
    Object obj;
    // 如果没有指定编码, 默认用 utf-8 编码
    Charset charset;
    if (mediaType != null) {
      charset = mediaType.charset().or(Charset.forName("utf-8"));
    } else {
      charset = Charset.forName("utf-8");
    }
    if (String.class == clazz) {
      obj = readString(stream, charset);
    } else if (List.class.isAssignableFrom(clazz)) {
      obj = Jsons.decodeList(readString(stream, charset), clazz);
    } else if (clazz == byte[].class) {
      obj = readFullBytes(stream);
    } else if (clazz == FileMemoryInputStream.class) {
      final String fileName;
      int i = request.getUrl().lastIndexOf("/");
      if (i > 0) {
        String sec = request.getUrl().substring(i + 1);
        int paramIndex = sec.indexOf("?");
        if (paramIndex > 0) {
          fileName = sec.substring(0, paramIndex);
        } else {
          fileName = sec;
        }
      } else {
        fileName =
          new ObjectId().toHexString() + "." + mediaType != null ? mediaType.subtype() : "unknown";
      }
      obj = new FileMemoryInputStream(stream, fileName, contentLength);
    } else if (InputStream.class.isAssignableFrom(clazz)) {
      obj = stream;
    } else {
      obj = Jsons.decode(readString(stream, charset), clazz);
    }
    return (T) obj;
  }

  public static String readString(InputStream in, Charset c) {
    try (Buffer source = new Buffer().readFrom(in)) {
      Charset charset = Util.bomAwareCharset(source, c);
      return source.readString(charset);
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  public static byte[] readFullBytes(InputStream in) {
    try (Buffer source = new Buffer().readFrom(in)) {
      return source.readByteArray();
    } catch (IOException e) {
      throw new ThymeException(e.getMessage(), e);
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  class FileMemoryInputStream extends InputStream {

    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    private InputStream inputStream;
    /**
     * 文件名
     */
    private String fileName;

    private State state;
    private final Long contentLength;

    FileMemoryInputStream(InputStream inputStream, String fileName, Long contentLength) {
      this.inputStream = inputStream;
      this.fileName = fileName;
      this.contentLength = contentLength;
    }

    @Override
    public int read() throws IOException {
      return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
      int read = inputStream.read(b);
      if (state != null) {
        state.update(read);
      }
      return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return inputStream.read(b, off, len);
    }

    public FileMemoryInputStream rename(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public String getFileName() {
      return fileName;
    }

    public void save() {
      save(null, null);
    }

    public void save(Consumer<State> consumer) {
      save(null, consumer);
    }

    public void save(String path) {
      save(path, null);
    }

    public void save(String path, Consumer<State> consumer) {
      if (consumer != null) {
        state = new State(contentLength);
        executorService.execute(() -> {
          copy(path);
          state.end();
        });
        long preReceivedSize = 0;
        while (true) {
          Long receivedSize = state.getReceivedSize();
          state.setReceiveRate((receivedSize - preReceivedSize) * 2);
          preReceivedSize = receivedSize;
          consumer.accept(state);
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            // ignore
          }
          if (state.isEnd()) {
            consumer.accept(state);
            break;
          }
        }
      } else {
        copy(path);
      }
    }

    private void copy(String path) {
      String newPath = path;
      if (newPath == null) {
        newPath = "";
      }
      try {
        Path filePath = Paths.get(newPath, fileName);
        if (Files.exists(filePath)) {
          int index = fileName.lastIndexOf(".");
          String name = fileName.substring(0, index);
          int i = 1;
          while (true) {
            filePath = Paths.get(newPath,
              name + "(" + i + ")" + fileName.substring(index));
            if (!Files.exists(filePath)) {
              break;
            } else {
              i++;
            }
          }
        }
        Files.copy(this, filePath);
      } catch (NoSuchFileException e) {
        createDirectory(newPath);
        save(newPath, null);
      } catch (IOException e) {
        throw new ThymeException(e.getMessage(), e);
      }
    }

    private void createDirectory(String path) {
      Path parent = Paths.get(path);
      boolean directory = Files.isDirectory(parent);
      if (!directory) {
        try {
          Files.createDirectories(parent);
        } catch (IOException ex) {
          throw new ThymeException(ex.getMessage(), ex);
        }
      }
    }

    /**
     * @author yixi
     * @since 1.0.0
     */
    public static class State {

      private Long startTime; // 下载开始时间
      private Long totalSize; // 文件总大小
      private Long receivedSize = 0L; // 已经下载的大小
      private Long endTime; // 完成下载时间

      private Long receiveRate = 0L; // 接收速度

      State(Long totalSize) {
        this.totalSize = totalSize;
        startTime = System.currentTimeMillis();
      }

      public void update(int size) {
        receivedSize += size;
      }

      public void end() {
        endTime = System.currentTimeMillis();
      }

      public boolean isEnd() {
        return endTime != null;
      }

      public Long getStartTime() {
        return startTime;
      }

      public Long getTotalSize() {
        return totalSize;
      }

      public Long getReceivedSize() {
        return receivedSize;
      }

      public Long getEndTime() {
        return endTime;
      }

      void setReceiveRate(Long receiveRate) {
        this.receiveRate = receiveRate;
      }

      public Long getReceiveRate() {
        return receiveRate;
      }
    }
  }

  /**
   * @author yixi
   * @since 1.0.0
   */
  class ThymeResponse {

    private HttpRequest request;
    private int code;
    private InputStream dataStream;
    private HttpRequest.Multimap headers;
    private String requestUrl;
    private Long contentLength;
    private MediaType mediaType;
    private Integer responseTime; // 响应时间

    public HttpRequest getRequest() {
      return request;
    }

    public void setRequest(HttpRequest request) {
      this.request = request;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int code) {
      this.code = code;
    }

    public InputStream getDataStream() {
      return dataStream;
    }

    public void setDataStream(InputStream dataStream) {
      this.dataStream = dataStream;
    }

    public HttpRequest.Multimap getHeaders() {
      return headers;
    }

    public void setHeader(String name, String value) {
      if (this.headers == null) {
        this.headers = new Multimap();
      }
      this.headers.set(name, value);
    }

    public void addHeader(String name, String value) {
      if (this.headers == null) {
        this.headers = new Multimap();
      }
      this.headers.add(name, value);
    }

    public void setHeaders(HttpRequest.Multimap headers) {
      this.headers = headers;
    }

    public String getRequestUrl() {
      return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
      this.requestUrl = requestUrl;
    }

    public Long getContentLength() {
      return contentLength;
    }

    public void setContentLength(Long contentLength) {
      this.contentLength = contentLength;
    }

    public MediaType getMediaType() {
      return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
      this.mediaType = mediaType;
    }

    public Integer getResponseTime() {
      return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
      this.responseTime = responseTime;
    }

    public String readString() {
      return readData(String.class);
    }

    public <T> T readData(Class<T> clazz) {
      return ThymeHttpClient.read(request, dataStream, contentLength, mediaType, clazz);
    }
  }
}
