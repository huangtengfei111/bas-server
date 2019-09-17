package app.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface XlsxExportable {

  public static final String GROUP_BY_BILL_TYPE_FILE = "计费类型报表";
  public static final String[] GROUP_BY_BILL_TYPE_HEADER = { "类型", "次数" };
  public static final String GROUP_BY_OWNER_LAC_FILE = "小区号lac报表";
  public static final String[] GROUP_BY_OWNER_LAC_HEADER = { "lac", "联系次数" };
  public static final String GROUP_BY_OWNER_NUM_FILE = "本方号码";
  public static final List<String> GROUP_BY_OWNER_NUM_HEADER =
      new ArrayList<String>(Arrays.asList("本方号码", "号码级别", "关联度", "联系人个数",
          "联系次数", "总通话时间s", "通话时间", "首次时间", "末次", "首末相距", "使用天数", "未使用天数"));

  default void renderXlsx(List<String> headers, LinkedHashMap<Object, List<Map>> rs, OutputStream out)
      throws IOException {

    String sheetName = "表格1";
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(sheetName);

    Font headerFont = workbook.createFont();

    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());


    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);
    headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
    // item??啥????
    CellStyle itemCellStyle = workbook.createCellStyle();
    itemCellStyle.setAlignment(HorizontalAlignment.CENTER);

    Row hearderRow = sheet.createRow(0);
    // generate header
    for (int i = 0; i < headers.size(); i++) {
      Cell cell = null;
      cell = hearderRow.createCell(i);
      cell.setCellStyle(headerCellStyle);
      cell.setCellValue(headers.get(i));
      sheet.setColumnWidth(i, headers.get(i).getBytes().length * 2 * 250);
    }

    int columnCount = hearderRow.getLastCellNum();
    Set<Object> lmks = rs.keySet();// 获取rs的key的集合
    if (hearderRow != null) {
      int next = 1;// 行数
      for (Object lmk : lmks) {// 遍历rs的key集合
        List<Map> lm = rs.get(lmk);// 根据key获取对应的List<Map>
        Row newRow = sheet.createRow(next);// 获取设值的那一行
        for (Map m : lm) {// 遍历List<Map> lm 获取需要设值的单条数据
          int innerNext = 0;// 单元格数
          for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {//逐条去除map键值对
            String mapKey = hearderRow.getCell(columnIndex).toString().trim();// 获取表头的值
            if (!m.containsKey(mapKey)) {// 判断map中是否有与表头对应的key
              innerNext++;
              continue;// 若无,单元格数+1,返回循环
            } else {
              Cell cell = newRow.createCell(innerNext);
              cell.setCellValue(
                  m.get(mapKey) == null ? null : m.get(mapKey).toString());
              innerNext++;// 若有,将数据设入单元格中,单元格数+1
            }
          }
        }
        next++;// 行数+1
      }
    }


    workbook.write(out);// 将工作薄内容读入输出流
    workbook.close();// 关闭流对象

  }

  default void renderXlsx(String[] headers, Map rs, OutputStream out)
      throws IOException {
    String sheetName = "表格1";// 工作表的名称???名称怎么来?
    Workbook workbook = new XSSFWorkbook();// 创建一个工作薄对象
    Sheet sheet = workbook.createSheet(sheetName);// 为工作薄创建一个工作表对象

    Font headerFont = workbook.createFont();// 为工作薄创建一个字体对象
    // 设置字体的格式,大小,颜色
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());

    // 为工作薄创建一个基本类型对象
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);// 将字体格式设进基本类型
    headerCellStyle.setAlignment(HorizontalAlignment.CENTER);// 设置水平方向居中对其
    // item??啥????
    CellStyle itemCellStyle = workbook.createCellStyle();
    itemCellStyle.setAlignment(HorizontalAlignment.CENTER);

    Row hearderRow = sheet.createRow(0);// 获取工作表的第一行,即表头
    // generate header
    for (int i = 0; i < headers.length; i++) {// 遍历传进的表头参数列表,将其依次设入第一行的单元格中
      Cell cell = null;
      cell = hearderRow.createCell(i);
      cell.setCellStyle(headerCellStyle);
      cell.setCellValue(headers[i]);
      sheet.setColumnWidth(i, headers[i].getBytes().length * 2 * 250);
    }

    int columnCount = hearderRow.getLastCellNum();// 获得表头的列数
    Set mapKeys = rs.keySet();// 获取map所有的键


    if (hearderRow != null) {
      int next = 1;
      for (Object mapKey : mapKeys) {
        Row newRow = sheet.createRow(next);
        String key = mapKey.toString().trim();
        for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {// 遍历表头
          Cell cell = newRow.createCell(columnIndex);
          if (columnIndex == 0) {
            cell.setCellValue(key);
          } else {
            cell.setCellValue(rs.get(key) == null ? null : rs.get(key).toString());
        }
        }
        next++;
      }
    }

    workbook.write(out);// 将工作薄内容读入输出流
    workbook.close();// 关闭流对象

  }

  default void renderXlsx(String[] headers, List<Map> rs, OutputStream out) throws IOException {
    String sheetName = "表格1";// 工作表的名称???名称怎么来?
    Workbook workbook = new XSSFWorkbook();// 创建一个工作薄对象
    Sheet sheet = workbook.createSheet(sheetName);// 为工作薄创建一个工作表对象

    Font headerFont = workbook.createFont();// 为工作薄创建一个字体对象
    // 设置字体的格式,大小,颜色
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());

    // 为工作薄创建一个基本类型对象
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);// 将字体格式设进基本类型
    headerCellStyle.setAlignment(HorizontalAlignment.CENTER);// 设置水平方向居中对其
    // item??啥????
    CellStyle itemCellStyle = workbook.createCellStyle();
    itemCellStyle.setAlignment(HorizontalAlignment.CENTER);
    
    Row hearderRow = sheet.createRow(0);// 获取工作表的第一行,即表头
    // generate header
    for (int i = 0; i < headers.length; i++) {// 遍历传进的表头参数列表,将其依次设入第一行的单元格中
      Cell cell = null;
      cell = hearderRow.createCell(i);
      cell.setCellStyle(headerCellStyle);
      cell.setCellValue(headers[i]);
      sheet.setColumnWidth(i, headers[i].getBytes().length * 2 * 250);
    }

    int columnCount = hearderRow.getLastCellNum();// 获得表头的列数
    if (hearderRow != null) {
      for (int rowId = 0; rowId < rs.size(); rowId++) {
        Map map = rs.get(rowId);// 获取需要设值的单条数据
        Row newRow = sheet.createRow(rowId + 1);// 获取设值的那一行
        for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {// 遍历表头
          //获取mapKey(即表头的值,作为设值的标准)
          String mapKey = hearderRow.getCell(columnIndex).toString().trim();
          Cell cell = newRow.createCell(columnIndex);// 创建需要设值的单元格对象
          cell.setCellValue(map.get(mapKey) == null ? null : map.get(mapKey).toString());//判断map中是否有符合mapKey的map,若有则设值,若无,则设空
        }
      }
    }
//    if (isTowRow) {
//      generateTowRow(headList, maps, sheet, itemCellStyle);
//    } else {
//      generateMulRow(headList, (LinkedHashMap<Object, Object>) maps, sheet, itemCellStyle, itemCellStyle);
//    }
    workbook.write(out);// 将工作薄内容读入输出流
    workbook.close();// 关闭流对象
  }
}
