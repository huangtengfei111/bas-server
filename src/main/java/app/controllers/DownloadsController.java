package app.controllers;

import static org.javalite.common.Collections.map;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javalite.activeweb.annotations.GET;
/**
 * @author 
 */
@SuppressWarnings("unchecked")
public class DownloadsController extends APIController {

  public void index(){
    render();
  }

  @GET
  public void venNumbers() throws IOException {

    String fname = "虚拟网模板";
    String fileName = URLEncoder.encode(fname, StandardCharsets.UTF_8.toString());

    OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                    map("Content-Disposition", 
                                        "attachment;filename=" + fileName + ".xlsx"), 
                                    200);
    
    String[] columns = { "长号", "短号", "虚拟网" };

    
    String[] row1 = { "13105717754", "62360", "开发虚拟网" };
    String[] row2 = { "13214527451", "62457", "测试虚拟网" };
    List<String[]> rows = Arrays.asList(row1, row2);

    Workbook workbook = new XSSFWorkbook();
    
    buildXlsxFile(workbook, "示例", columns, rows);

    workbook.write(out);     
    // Closing the workbook
    workbook.close();                   

    // no need to close the stream, container will do that    
  }

  @GET
  public void pnumLabels() throws IOException {
    
  	String fname = "号码标注模板";
  	String fileName = URLEncoder.encode(fname, StandardCharsets.UTF_8.toString());
  	
  	OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                    map("Content-Disposition", 
                                        "attachment;filename=" + fileName + ".xlsx"), 
                                    200);
  	
    String[] columns = { "长号", "标注", "备注" };
  	
    String[] row1 = { "19877792773", "张三", "测试" };
    String[] row2 = { "13387629018", "李四", "开发" };
    String[] row3 = { "19998782837", "黄五", "开发" };
    List<String[]> rows = Arrays.asList(row1, row2, row3);
  	
  	Workbook workbook = new XSSFWorkbook();
  	
  	buildXlsxFile(workbook, "示例", columns, rows);
  	
  	workbook.write(out);
  	workbook.close();
  }
  
  @GET
  public void relNumbers() throws IOException {
  	
  	String fname = "亲情网模板";
  	String fileName = URLEncoder.encode(fname, StandardCharsets.UTF_8.toString());
  	
  	OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                    map("Content-Disposition", 
                                        "attachment;filename=" + fileName + ".xlsx"), 
                                    200);
  	
    String[] columns = { "长号", "短号", "亲情网" };
  	
    String[] row1 = { "19877773", "111", "测试亲情网" };
    String[] row2 = { "74525633", "888", "开发亲情网" };
  	List<String[]> rows = Arrays.asList(row1, row2);
  	
  	Workbook workbook = new XSSFWorkbook();
  	
  	buildXlsxFile(workbook, "示例", columns, rows);
  	
  	workbook.write(out);
  	workbook.close();
  }
  
  @GET
  public void ctLabels() throws IOException {
    
  	String fname = "基站标注模板";
  	String fileName = URLEncoder.encode(fname, StandardCharsets.UTF_8.toString());
  	
  	OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                    map("Content-Disposition", 
                                        "attachment;filename=" + fileName + ".xlsx"), 
                                    200);
  	
    String[] columns = { "基站代码", "标注名称", "备注" };
  	
    String[] row1 = { "(16进制)LAC:CI:MNC", "某上班地点", "某大楼" };
    String[] row2 = { "1000:166748:0", "某的家", "某某小区" };
  	
  	List<String[]> rows = Arrays.asList(row1, row2);
  	
  	Workbook workbook = new XSSFWorkbook();
  	
  	buildXlsxFile(workbook, "示例", columns, rows);
  	
  	workbook.write(out);
  	workbook.close();
  }

  @GET
  public void citizenBook() throws IOException {
    
    String fname = "人员通讯录";
    String fileName = URLEncoder.encode(fname, StandardCharsets.UTF_8.toString());
    
    OutputStream out = outputStream("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                    map("Content-Disposition", 
                                        "attachment;filename=" + fileName + ".xlsx"), 
                                    200);
    
    String[] columns = { "身份证", "姓名","单位", "职务", "私人/号码", "工作/号码", "公司/地址", "住宅/地址" };
    String[] row0 =
        { "可以不填写", "必须填写", "可以不填写", "可以不填写", "`号码`为列名不可改动，`/`前面为备注，可改动",
            "号码可以有多个", "`地址`为列名不可改动，`/`前面为备注，可改动", "地址可以有多个" };
    String[] row1 = { "998818189299122", "张三", "阿里", "经理", "88882773382",
        "991991222", "上海市嘉定区安亭镇新源路", "速世盛路出口" };
    String[] row2 = { "992992838828838", "刘力", "第一中学", "教导主任", "99182771",
        "9999923772", "上海市嘉定区安亭镇新源路", "静路与胜巷路路口北888米" };
    
    List<String[]> rows = Arrays.asList(row0, row1, row2);
    
    Workbook workbook = new XSSFWorkbook();
    
    buildXlsxFile(workbook, "示例", columns, rows);
    
    workbook.write(out);
    workbook.close();
  }

  /**
   *
   */
  private void buildXlsxFile(Workbook workbook, String sheetName, String[] columns, List<String[]> rows) {
    /* CreationHelper helps us create instances of various things like DataFormat, 
       Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
    CreationHelper createHelper = workbook.getCreationHelper();

    // Create a Sheet
    Sheet sheet = workbook.createSheet(sheetName);
    
    // Create a Font for styling header cells
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());

    // Create a CellStyle with the font
    CellStyle headerCellStyle = workbook.createCellStyle();
    headerCellStyle.setFont(headerFont);

    // Create a Row
    Row headerRow = sheet.createRow(0);

    // Create cells
    for(int i = 0; i < columns.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(columns[i]);
      cell.setCellStyle(headerCellStyle);
    }

    // Create Cell Style for formatting Date
    // CellStyle dateCellStyle = workbook.createCellStyle();
    // dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

    // Create Other rows and cells with rows data
    int rowNum = 1;
    for(String[] rowVal: rows) {
      Row row = sheet.createRow(rowNum++);

      int i = 0;
      for(String cVal: rowVal) {
        row.createCell(i++)
              .setCellValue(cVal);
      }
    }

    // Resize all columns to fit the content size
    for(int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
    }    
  }
}