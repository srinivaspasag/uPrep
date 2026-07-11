package com.vedantu.board.utils;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

public class XLParserUtils {

	public static String getCellValueAsString(Cell cell) {
		return getCellValueAsString(cell, null);
	}

	public static String getCellValueAsString(Cell cell, String dateFormat) {
		String value = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			value = cell.getRichStringCellValue().getString();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				if (StringUtils.isNotEmpty(dateFormat)) {
					value = DateFormatUtils.format(date, dateFormat);
				} else {
					value = cell.getDateCellValue().toString();
				}
			} else {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				value = String.valueOf(cell.getStringCellValue());
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			value = cell.getCellFormula();
			break;
		default:
			value = "";
		}
		return StringUtils.trim(value);
	}
}
