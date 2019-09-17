package app.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javalite.activejdbc.Model;

import app.exceptions.InvalidFieldValueException;

public class ExcelFileHelper {

	public static <T extends Model> List<T> extract(Map<String, String> header, 
		                                              InputStream inStream, 
		                                              Class<? extends T> modelClass) 
	throws Exception {
		return extract(header, inStream, modelClass, 1, null);
	}

	public static <T extends Model> List<T> extract(Map<String, String> header, 
		                                              InputStream inStream, 
		                                              Class<? extends T> modelClass,
		                                              Consumer<T> consumer) 
	throws Exception {
		return extract(header, inStream, modelClass, 1, consumer);
	}

	public static <T extends Model> List<T> extract(Map<String, String> header, 
		                                              InputStream inStream, 
		                                              Class<? extends T> modelClass, 
		                                              int maxSheet, 
		                                              Consumer<T> consumer) 
	throws Exception {
    
    // Class.forName(modelClass.getName());
    // String tableName = Registry.instance().getTableName(modelClass);
    // MetaModel metaModel = metaModelFor(tableName);
     
    Workbook workbook = new XSSFWorkbook(inStream);
    
		Sheet sheet = workbook.getSheetAt(0);

		// build header
		HashMap<Integer, String> headerMap = new HashMap<>();

		int headerRowIdx = sheet.getFirstRowNum();
		int headerCells = sheet.getRow(headerRowIdx).getPhysicalNumberOfCells();

		Row headerRow = sheet.getRow(headerRowIdx);
		header.forEach((k, v) -> {
			for (int i = 0; i < headerCells; i++) {
				Cell cell = headerRow.getCell(i);
				String text =  cell.getStringCellValue();
				if (text != null && text.startsWith(k)){
				  headerMap.put(i, v);
				}
			}
		});

		// process rows
		List<T> collector = new ArrayList<>();
		int maxRow = sheet.getPhysicalNumberOfRows();
		DataFormatter df = new DataFormatter();

		for (int i=(headerRowIdx + 1); i<maxRow; i++) {
			Row row = sheet.getRow(i);
			// https://docs.oracle.com/javase/tutorial/reflect/member/ctorInstance.html
			T model = modelClass.newInstance();

			headerMap.forEach((idx, field) -> {
				String fieldValue = df.formatCellValue(row.getCell(idx));
				model.set(field, fieldValue);
			});

			if (consumer != null) {
				consumer.accept(model);
			}
      model.validate();
      if (model.errors() != null && model.errors().size() > 0) {
        throw new InvalidFieldValueException(model.errors());
      }
			collector.add(model);
		}
		
		return collector;
	}
}