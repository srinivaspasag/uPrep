package com.lms.common.utils;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.util.StringUtils;

public class XLParserUtils {

	public static String getCellValueAsString(Cell cell) {
		return getCellValueAsString(cell, null);
	}

	public static String getCellValueAsString(Cell cell, String dateFormat) {
		String value = null;
		switch (cell.getCellType()) {
		case STRING:
			value = cell.getRichStringCellValue().getString();
			break;
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				if (!StringUtils.isEmpty(dateFormat)) {
					//value = DateFormatUtils.format(date, dateFormat);
				} else {
					value = cell.getDateCellValue().toString();
				}
			} else {
				cell.setCellType(CellType.STRING);
				value = String.valueOf(cell.getStringCellValue());
			}
			break;
		case BOOLEAN:
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		case FORMULA:
			value = cell.getCellFormula();
			break;
		default:
			value = "";
		}
		return value.trim();
	}
}
