package app.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.models.Citizen;
import app.models.CitizenAddress;
import app.models.CitizenPhone;

public class PubUtilitiesFileHelper {

	private static final Logger log = LoggerFactory.getLogger(PubUtilitiesFileHelper.class);
  public static final String F_SOCIAL_NO = "social_no";
  public static final String F_NAME = "name";
  public static final String F_PHONE_NUM = "phone.num";
  public static final String F_VEN_NAME = "phone.ven_name";
  public static final String F_ADDR_LOC = "address.loc";

	public static List<Citizen> read(Map<String, String> header, InputStream inStream, int maxSheet) 
	throws IOException {
    Workbook workbook = new XSSFWorkbook(inStream);
    
    Sheet sheet = workbook.getSheetAt(0);

    // build header (<cell index, attribute, memo, ref> )
    HashMap<Integer, List<String>> headerMap = new HashMap<>();
    int venNameIdx = -1;
    int headerRowIdx = sheet.getFirstRowNum();
    int headerCells = sheet.getRow(headerRowIdx).getPhysicalNumberOfCells();

    Row headerRow = sheet.getRow(headerRowIdx);

		// header.forEach((k, v) -> {
    for (Map.Entry<String, String> entry : header.entrySet()) {
      String k = entry.getKey();
      String v = entry.getValue();
 
			boolean usingStartWith = false;
      if (k.startsWith("^")) {
				usingStartWith = true;
				k = k.substring(1);
			}
			for (int i = 0; i < headerCells; i++) {
        Cell cell = headerRow.getCell(i);
        if (cell != null) {
          String text = cell.getStringCellValue();
          if (text != null) {
            if (usingStartWith && text.endsWith(k)) {
              if (F_VEN_NAME.equals(v)) {
                venNameIdx = i;
              }
              // retrieve the memo
              String[] s2 = text.split("/", 2);
              if (s2.length > 1) {
                headerMap.put(i, Arrays.asList(v, s2[0]));
              } else {
                headerMap.put(i, Arrays.asList(v, null));
              }
            } else if (k.equals(text)) {
              if (F_VEN_NAME.equals(v)) {
                venNameIdx = i;
              }
              headerMap.put(i, Arrays.asList(v, null));
            }
          }
        }
			}
		}

		log.debug("Headmap for excel: {}", headerMap);

		// process rows
    List<Citizen> collector = new ArrayList<>();
    int maxRow = sheet.getPhysicalNumberOfRows();
    DataFormatter df = new DataFormatter();

    for (int i = (headerRowIdx + 1); i < maxRow; i++) {
      Row row = sheet.getRow(i);

      Citizen citizen = new Citizen();
      List<CitizenPhone> phones = new ArrayList<>();
      List<CitizenAddress> addresses = new ArrayList<>();

      for (Map.Entry<Integer, List<String>> entry : headerMap.entrySet()) {
        int idx = entry.getKey();
        List<String> list = entry.getValue();

        String field = list.get(0);
        String memo = list.get(1);
        String fieldValue = df.formatCellValue(row.getCell(idx));
        String model = null;

        String[] items = field.split("\\.");
        if (items != null && items.length > 1) {
          model = items[0];
          field = items[1];
        }

        if ("phone".equals(model)) {
          if (("num".equals(field))) {
            CitizenPhone phone = new CitizenPhone();
            phone.set("num", fieldValue);
            phone.set("memo", memo);

            phones.add(phone);
          } else if ("ven_num".equals(field)) {
            CitizenPhone phone = new CitizenPhone();
            phone.set("num", fieldValue);
            phone.set("memo", memo);
            String venName = df.formatCellValue(row.getCell(venNameIdx));
            phone.set("ven_name", venName);
            phones.add(phone);
          }
        } else if ("address".equals(model)) {
          if ("loc".equals(field)) {
            CitizenAddress address = new CitizenAddress();
            address.set("loc", fieldValue);
            address.set("memo", memo);

            addresses.add(address);
          }
        } else {
          citizen.set(field, fieldValue);
        }
      }

			citizen._setPhones(phones);
			citizen._setAddresses(addresses);
			collector.add(citizen);
		}
		
		return collector;
  }
}