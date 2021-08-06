package org.yixi.thyme.core;

import com.monitorjbl.xlsx.StreamingReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Builder;
import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * @author yixi
 * @since 1.0.0
 */
public interface StructuredReader {

  ReadStatus read(Function<Map> fn);

  ReadStatus read(ReadOptions readOptions, Function<Map> fn);

  <T> ReadStatus read(ReadOptions readOptions, Class<T> clazz, Function<T> fn);

  void close();

  /**
   * @author mark
   */
  abstract class AbstractExcelReader implements StructuredReader {

    protected final AtomicReference<Boolean> closed = new AtomicReference<>(false);

    protected Workbook workbook;

    @Override
    public ReadStatus read(Function<Map> fn) {
      return read(ReadOptions.builder().build(), fn);
    }

    @Override
    public ReadStatus read(ReadOptions readOptions, Function<Map> fn) {
      return read0(readOptions, fn);
    }

    @Override
    public <T> ReadStatus read(ReadOptions readOptions, Class<T> clazz, Function<T> fn) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
      if (workbook != null) {
        closed.set(true);
      }
    }

    protected ReadStatus read0(ReadOptions readOptions, Function<Map> fn) {
      try {
        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map header = null;
        if (readOptions.isHead() && rowIterator.hasNext()) {
          header = new HashMap();
          Row row = rowIterator.next();
          int i = 0;
          for (Cell cell : row) {
            header.put(i++, cell.getStringCellValue());
          }
        }
        int limit = Integer.MAX_VALUE;
        if (readOptions.getLimit() > 0) {
          limit = readOptions.getLimit();
        }
        ReadStatus readStatus = new ReadStatus();
        if (lastRowNum > 0) {
          readStatus.setTotal(lastRowNum);
        } else {
          readStatus.setTotal(-1);
        }
        // skip 逻辑处理
        int skip = readOptions.getSkip();
        while (skip-- > 0 && rowIterator.hasNext()) {
          rowIterator.next();
        }
        int i = readOptions.isHead() ? 1 : 0;
        for (; rowIterator.hasNext(); i++) {
          if (i == limit || closed.get()) {
            break;
          }
          Row row = rowIterator.next();
          Map data = new LinkedHashMap();
          int colSize = header != null ? header.size() : row.getLastCellNum();
          for (int j = 0; j < colSize; j++) {
            Cell cell = row.getCell(j);
            if (cell == null) {
              continue;
            }
            Object key = header != null ? header.get(j) : j + "";
            if (cell.getCellType() == CellType.NUMERIC) {
              if (HSSFDateUtil.isCellDateFormatted(cell)) {
                data.put(key, cell.getDateCellValue());
              } else {
                data.put(key, cell.getNumericCellValue());
              }
            } else if (cell.getCellType() == CellType.STRING) {
              String string = cell.getStringCellValue();
              data.put(key, string != null ? string.trim() : "");
            } else if (cell.getCellType() == CellType.BOOLEAN) {
              data.put(key, cell.getBooleanCellValue());
            } else if (cell.getCellType() == CellType.BLANK) {
              data.put(key, "");
            } else {
              throw new UnsupportedOperationException("cellType: " + cell.getCellType());
            }
          }
          fn.accept(lastRowNum, i + 1, data);
        }
        readStatus.setRead(i - 1);
        return readStatus;
      } finally {
        try {
          workbook.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  /**
   * @author yixi
   * @since 1.1.2
   */
  @Data
  class ReadStatus {

    /**
     * 总记录数
     */
    private long total;
    /**
     * 总读取
     */
    private long read;
    /**
     * 失败
     */
    private long fail;
  }


  /**
   * @author yixi
   * @modifier mark
   */
  class XlsxStreamer extends AbstractExcelReader implements StructuredReader {


    public XlsxStreamer(String path) {
      workbook = StreamingReader.builder().bufferSize(4 * 1024).open(new File(path));
    }

    public XlsxStreamer(InputStream in) {
      workbook = StreamingReader.builder().bufferSize(4 * 1024).open(in);
    }

    @Override
    public void close() {
      super.close();
    }
  }

  /**
   * @author mark
   */
  class POIExcelReader extends AbstractExcelReader implements StructuredReader {

    public POIExcelReader(String path) {
      try {
        this.workbook = WorkbookFactory.create(new File(path));
      } catch (IOException e) {
        throw Thyme.ex(e);
      }
    }

    public POIExcelReader(InputStream in) {
      try {
        this.workbook = WorkbookFactory.create(in);
      } catch (IOException e) {
        throw Thyme.ex(e);
      }
    }
  }

  /**
   * @author yixi
   */
  class CsvReader implements StructuredReader {

    protected final AtomicReference<Boolean> closed = new AtomicReference<>(false);

    private final ICsvListReader csvListReader;

    public CsvReader(String path) {
      try {
        csvListReader = new CsvListReader(new FileReader(path), CsvPreference.STANDARD_PREFERENCE);
      } catch (Exception e) {
        throw Thyme.ex(e);
      }
    }

    public CsvReader(InputStream in) {
      try {
        csvListReader = new CsvListReader(new InputStreamReader(in),
          CsvPreference.STANDARD_PREFERENCE);
      } catch (Exception e) {
        throw Thyme.ex(e);
      }
    }

    @Override
    public ReadStatus read(Function<Map> fn) {
      return read(ReadOptions.builder().build(), fn);
    }

    @Override
    public ReadStatus read(ReadOptions readOptions, Function<Map> fn) {
      try {
        ReadStatus readStatus = new ReadStatus();
        String[] headers = null;
        if (readOptions.isHead()) {
          headers = csvListReader.getHeader(true);
        }
        List<String> list;
        int limit = Integer.MAX_VALUE;
        if (readOptions.getLimit() > 0) {
          limit = readOptions.getLimit();
        }
        // skip 处理逻辑
        int skip = readOptions.getSkip();
        while (skip-- > 0 && csvListReader.read() != null) {
        }
        int l = 0;
        while (!closed.get() && l++ < limit && (list = csvListReader.read()) != null) {
          Map data = new LinkedHashMap();
          for (int i = 0; i < list.size(); i++) {
            String value = list.get(i);
            data.put(headers != null ? headers[i] : i + "", value != null ? value.trim() : null);
          }
          fn.accept(0, csvListReader.getLineNumber(), data);
        }
        return readStatus;
      } catch (Exception e) {
        throw Thyme.ex(e);
      }
    }

    @Override
    public <T> ReadStatus read(ReadOptions readOptions, Class<T> clazz, Function<T> fn) {
      throw Thyme.exUnsupported("");
    }

    @Override
    public void close() {
      closed.set(true);
    }
  }

  /**
   * @author yixi
   */
  @Data
  @Builder
  class ReadOptions {

    /**
     * 跳过读取行数
     */
    private int skip;

    /**
     * 最大读取行数
     */
    private int limit;

    /**
     * 是否有表头
     */
    private boolean head;

    /**
     * 读取所有 sheet
     */
    private boolean allSheet;

  }

  /**
   * @author yixi
   */
  interface Function<T> {

    void accept(int total, int currentNum, T t);
  }
}

