package app.controllers;

import static org.javalite.common.Collections.map;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReportController extends APIController implements XlsxExportable {

  public void renderExcel(String[] headers, String viewVarName,
      String outputFileName) throws IOException {

  }
  
  public void renderExcel(List<String> headers, LinkedHashMap<Object, List<Map>> rs, String outputFileName)
      throws IOException {

    String fileName =
        URLEncoder.encode(outputFileName, StandardCharsets.UTF_8.toString());
    OutputStream out = outputStream(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        map("Access-Control-Expose-Headers", "Content-Disposition",
            "Content-Disposition", "attachment;filename=" + fileName),
        200);

    renderXlsx(headers, rs, out);

  }

  public void renderExcel(String[] headers, Map rs, String outputFileName)
      throws IOException {

    String fileName =
        URLEncoder.encode(outputFileName, StandardCharsets.UTF_8.toString());
    OutputStream out = outputStream(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        map("Access-Control-Expose-Headers", "Content-Disposition",
            "Content-Disposition", "attachment;filename=" + fileName),
        200);

    renderXlsx(headers, rs, out);

  }

  public void renderExcel(String[] headers, List<Map> rs,
      String outputFileName) throws IOException {
    String fileName =
        URLEncoder.encode(outputFileName, StandardCharsets.UTF_8.toString());
    OutputStream out = outputStream(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        map("Access-Control-Expose-Headers", "Content-Disposition",
            "Content-Disposition", "attachment;filename=" + fileName),
        200);

    renderXlsx(headers, rs, out);
  }
  /**
   * 案例 renderXlsx(Arrays.asList("时长", "总数"), "时长表");
   * 
   * 
   * 该方法会根据传过来的类型判断是否是二维表 参数是Map,判断条件是 map的value 类型 
   * object instanceof LinkedHashMap 是否成立 成立会被认为不是二维表,其他情况默认二维表.
   * 
   * 当参数未null时 会return; 不做任何处理
   * 
   * 已知BUG : 1.里边的参数是通过 values().get("resultMap")获得的,可修改为传参.
   * 
   * @param headList       表头
   * @param outputFileName 表名字
   * @throws IOException
   */
  @Deprecated
  public void renderXlsx2(List<String> headList, String outputFileName) throws IOException {
    //是否是二维表
    boolean isTowRow=true;
    String fileName = URLEncoder.encode(outputFileName, StandardCharsets.UTF_8.toString());
    fileName = fileName + ".xlsx";
    OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    map("Access-Control-Expose-Headers", "Content-Disposition", "Content-Disposition",
                                        "attachment;filename=" + fileName),
                                    200);
       
    // map("Content-Disposition", "attachment;filename=" + fileName + ".xlsx", "",
    // ""), 200);

    Map<Object, Object> maps = (Map) values().get("resultMap");

    if (maps.size() == 0) {
      return;
    }
    
    // 判断表类型
    for (Object key : maps.keySet()) {
      Object object = maps.get(key);
      if (object instanceof LinkedHashMap) {
        isTowRow=false;
      } 
      break;
    }
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(outputFileName);
    // 创建字体
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());
    // 创建cell风格
    // header
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);
    headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
    // item
    CellStyle itemCellStyle = workbook.createCellStyle();
    itemCellStyle.setAlignment(HorizontalAlignment.CENTER);
     
    
    Row hearderRow = sheet.createRow(0);
    // 生成header
    for (int i = 0; i < headList.size(); i++) {
      Cell cell = null;
      cell = hearderRow.createCell(i);
      cell.setCellStyle(headerCellStyle);
      cell.setCellValue(headList.get(i));
      sheet.setColumnWidth(i, headList.get(i).getBytes().length*2*250);
    }
    if (isTowRow) {
      generateTowRow(headList, maps, sheet, itemCellStyle);
    }else {
      generateMulRow(headList, (LinkedHashMap<Object, Object>) maps, sheet, itemCellStyle, itemCellStyle);
    }
    workbook.write(out);
    workbook.close();

  }
/**
 * 
 * @param headList
 * @param maps  
 * @param sheet 单元表
 * @param firstCellStyle 首行格式
 * @param itemCellStyle 首行之后的格式
 */
  // 生成多行表
  @SuppressWarnings("unchecked")
  private void generateMulRow(List<String> headList, LinkedHashMap<Object, Object> maps, Sheet sheet,
      CellStyle firstCellStyle, CellStyle itemCellStyle) {
    // rowCount
    int rowc = 0;
    for (Entry<Object, Object> entry : maps.entrySet()) {
      // 从第一行开始插入数据
      // colCount
      int colc = 0;
     
      Row row = sheet.createRow(++rowc);
      Cell cell0 = row.createCell(colc++);
      cell0.setCellStyle(firstCellStyle);
      cell0.setCellValue(entry.getKey().toString());
      LinkedHashMap<Object, Object> valuesMap = (LinkedHashMap<Object, Object>) entry.getValue();
      for (Entry<Object, Object> itemEntry : valuesMap.entrySet()) {
        String key = itemEntry.getKey().toString();
        key = key.equals("min") ? "首次时间" : key;
        key = key.equals("max") ? "末次时间" : key;
        key = key.equals("count") ? "总计" : key;
        
        int num = headList.indexOf(key);
        
        if(num==-1)
          continue;
        
        
        Cell cell = row.createCell(num);
        // 设置style
        cell.setCellStyle(itemCellStyle);
        Object value = itemEntry.getValue();
        
        if (value instanceof Timestamp) {
          DateFormat df = new SimpleDateFormat("HH:mm:ss");
          Timestamp timeValue=(Timestamp)value;
          cell.setCellValue(df.format(timeValue));
        }else{
          cell.setCellValue(value.toString());
        }
          
      }

    }
  }
  
  /**
   * 
   * @param headList
   * @param maps
   * @param sheet
   * @param itemCellStyle 首行格式
   */
  //生成二维表
  private void generateTowRow(List<String> headList,Map<Object,Object> maps,Sheet sheet,CellStyle itemCellStyle) {
    int rowC=1;
    for (Object key : maps.keySet()) {
        Row row = sheet.createRow(rowC++);
        Cell cell=row.createCell(0);
        cell.setCellValue(key.toString());
        Cell cell1=row.createCell(1);
        Object value =maps.get(key);
        cell1.setCellValue(value.toString());
        //设置style
        cell.setCellStyle(itemCellStyle);
        cell1.setCellStyle(itemCellStyle);
      }
   }
  }
  
