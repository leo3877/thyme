package org.yixi.thyme.core;

import org.yixi.thyme.core.StructuredReader.CsvReader;
import org.yixi.thyme.core.StructuredReader.POIExcelReader;
import org.yixi.thyme.core.StructuredReader.ReadOptions;
import org.yixi.thyme.core.StructuredReader.XlsxStreamer;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * @author yixi
 * @since 1.0.0
 */
public class POIStructuredReaderTest {

  @Test
  public void test() {
    StructuredReader structuredReader = new XlsxStreamer("/Users/yixi/mobile.xlsx");
    // 默认读取所有
    structuredReader.read((total, currentNum, record) -> {
      System.out.printf("总记录数:" + total);
      System.out.printf("当前读取:" + currentNum);
      System.out.println(record);
    });
    structuredReader = new POIExcelReader("/Users/yixi/test.xls");
    // 带表头读取 1000 行
    ReadOptions readOptions = ReadOptions.builder().head(true).skip(1).limit(1000).build();
    structuredReader.read(readOptions, (total, currentNum, record) -> {
      System.out.println(record);
    });

    // 带表头读取 1000 行并转成 Document 对象
//    structuredReader.read("/Users/yixi/test.xlsx", readOptions, Document.class, (total, currentNum, record) -> {
//        System.out.println(record);
//      });

  }

  @Test
  public void testCsv() {
    StructuredReader structuredReader = new POIExcelReader("/Users/yixi/test.xlsx");
    ReadOptions readOptions = ReadOptions.builder().head(true).build();

    long start = System.currentTimeMillis();
    CsvWriter csvWriter = new CsvWriter();
    // 默认读取所有
    structuredReader.read(readOptions, (total, currentNum, record) -> {
      if (currentNum == 2) { // 当设置 header == true 时，数据是从第二行开始读取
        String[] headers = new String[record.size()];
        CellProcessor[] processors = new CellProcessor[record.size()];
        processors[0] = new NotNull();
        int i = 0;
        for (Object key : record.keySet()) {
          headers[i] = key.toString();
          if (i > 0) {
            processors[i] = new Optional();
          }
          i++;
        }
        csvWriter.init("test.csv", headers, processors);
      }
      csvWriter.write(record);
      if (total == currentNum) {
        System.out.println("总记录数:" + total);
        csvWriter.close();
      }
    });

    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  public void testReadCsv0() {
    try {
      readWithCsvListReader();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testReadCsv1() {
    CsvReader csvReader = new CsvReader(
      "/Users/yixi/IdeaProjects/cdp2/laike_87543488839681/Order_youzan_2020-06-01-10-18-34_278ab32ad3aee516c702b37ffe396d17.csv");
    csvReader.read(ReadOptions.builder().head(true).build(), (total, currentNum, map) -> {
      System.out.println("currentNum: " + currentNum + ", data: " + map);
    });
  }

  private void readWithCsvListReader() throws Exception {

    ICsvListReader listReader = null;
    try {
      listReader = new CsvListReader(new FileReader("/Users/yixi/IdeaProjects/cdp2/test.csv"),
        CsvPreference.STANDARD_PREFERENCE);

      String[] headers = listReader.getHeader(true);
      List<String> list;
      while ((list = listReader.read()) != null) {
        Map data = new HashMap();
        for (int i = 0; i < headers.length; i++) {
          data.put(headers[i], list.get(i));
        }
        System.out.println(String.format("lineNo=%s, rowNo=%s, list=%s",
          listReader.getLineNumber(), listReader.getRowNumber(), data));
      }

    } finally {
      if (listReader != null) {
        listReader.close();
      }
    }
  }

  /**
   * @author yixi
   */
  public static class CsvWriter {

    private String[] headers;
    private CellProcessor[] processors;
    private ICsvMapWriter mapWriter;

    public void init(String fileName, String[] headers, CellProcessor[] processors) {
      this.headers = headers;
      this.processors = processors;
      try {
        mapWriter = new CsvMapWriter(new FileWriter("target/" + fileName),
          CsvPreference.STANDARD_PREFERENCE);
        mapWriter.writeHeader(headers);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void write(Map data) {
      try {
        mapWriter.write(data, headers, processors);
      } catch (IOException e) {
        e.printStackTrace();
      }
      // write the customer maps
    }

    public void close() {
      try {
        if (mapWriter != null) {
          mapWriter.close();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }
}
