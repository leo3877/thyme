package org.yixi.thyme.core;

import com.google.common.collect.Lists;
import org.yixi.thyme.core.StructuredReader.XlsxStreamer;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;

/**
 * @author yixi
 */
public class ExcelWriter {

  private final List<CellOption> options;
  private final Workbook workbook;
  private final Sheet sheet;
  private boolean closed;
  private int rownum;

  public ExcelWriter() {
    this(null);
  }

  public ExcelWriter(List<CellOption> options) {
    this.options = options;
    this.workbook = new SXSSFWorkbook();
    this.sheet = createSheet(workbook, options);
  }

  public static void main(String[] args) {
    List<CellOption> cellOptions = new ArrayList<>();
    cellOptions.add(CellOption.create("姓名", 4000));
    cellOptions.add(CellOption.create("性别", 4000));
    cellOptions.add(CellOption.create("年龄", 0));
    ExcelWriter excelWriter = new ExcelWriter(cellOptions);
    excelWriter.writeRow(Lists.newArrayList("老李", "男", 18));
    excelWriter.writeRow(Lists.newArrayList("老何", "男", 28));
    //excelWriter.write("test.xlsx");
    StructuredReader structuredReader = new XlsxStreamer("/Users/yixi/phone.xlsx");
    // 默认读取所有
    structuredReader.read((total, currentNum, record) -> {
      System.out.printf("总记录数:" + total);
      System.out.printf("当前读取:" + currentNum);
      System.out.println(record);
    });

  }

  public void writeRow(List<Object> cells) {
    System.out.println("num: " + sheet.getLastRowNum());
    if (closed) {
      throw Thyme.ex("Writer has closed");
    }
    Row row = sheet.createRow(rownum);
    for (int i = 0; i < cells.size(); i++) {
      Cell cell = row.createCell(i);
      Object value = cells.get(i);
      if (value == null) {
        continue;
      }
      if (value instanceof String) {
        cell.setCellValue((String) value);
      } else if (value instanceof Number) {
        cell.setCellValue(((Number) value).doubleValue());
      } else if (value instanceof Boolean) {
        cell.setCellValue((Boolean) value);
      } else if (value instanceof Date) {
        cell.setCellValue((Date) value);
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat(Thyme.DATE_TIME_FORMAT));
        cell.setCellStyle(cellStyle);
      } else {
        throw new UnsupportedOperationException("不支持 type: " + value.getClass().getSimpleName());
      }
    }
    sheet.getLastRowNum();
    rownum++;
  }

  public void write(String path) {
    try {
      FileOutputStream out = new FileOutputStream(path);
      write(out);
      out.close();
    } catch (Exception e) {
      throw Thyme.ex(e);
    }
  }

  public void write(OutputStream out) {
    try {
      closed = true;
      workbook.write(out);
      workbook.close();
    } catch (Exception e) {
      throw Thyme.ex(e);
    }

  }

  private Sheet createSheet(Workbook workbook, List<CellOption> options) {
    Sheet sheet = workbook.createSheet();
    sheet.setDefaultRowHeight((short) 400);
    if (options == null) {
      return sheet;
    }
    Row head = sheet.createRow(0);
    for (int i = 0; i < options.size(); i++) {
      CellOption cellOption = options.get(i);
      Cell cell = head.createCell(i);
      cell.setCellValue(cellOption.getHeaderName());
      if (cellOption.getWidth() > 0) {
        sheet.setColumnWidth(i, cellOption.getWidth());
      }
      cell.setCellStyle(headCellStyle(sheet.getWorkbook()));
    }
    rownum++;
    return sheet;
  }

  private CellStyle headCellStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }

  /**
   * @author yixi
   */
  @Data
  public static class CellOption {

    private String headerName;

    private int width;

    private int height;

    public static CellOption create(String name, int width) {
      CellOption cellOption = new CellOption();
      cellOption.setWidth(width);
      cellOption.setHeaderName(name);
      return cellOption;
    }
  }
}