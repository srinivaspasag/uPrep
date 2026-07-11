package com.vedantu.xml.parsers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.xml.parsers.models.AbstractXMLDataModel;

public abstract class AbstractSheetHandaler extends DefaultHandler {

	private static final ALogger LOGGER = Logger
			.of(AbstractSheetHandaler.class);
	private SharedStringsTable sst;
	private Map<String, String> headerMap;
	private Map<String, String> data;
	protected String lastContents;
	protected boolean nextIsString;
	protected String uuid;
	protected String sheetName;
	protected int sheetId;

	public AbstractSheetHandaler() {
		this(null);
	}

	public AbstractSheetHandaler(SharedStringsTable sst) {
		this.sst = sst;
		this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
		this.data = new HashMap<String, String>();
	}

	public void setSst(SharedStringsTable sst) {
		this.sst = sst;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setSheetId(int sheetId) {
		this.sheetId = sheetId;
		colChar = null;
		columnName = null;
	}

	private String columnName = null;
	private boolean isFirstRow = false;
	private String colChar = null;
	private int rowNo = 0;

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		// c => cell
		if (name.equals("row")) {
			String rValue = attributes.getValue("r");
			rowNo++;
			isFirstRow = false;
			data = new HashMap<String, String>();
			if (rValue.equals("1")) {
				isFirstRow = true;
				headerMap = new HashMap<String, String>();
			}
		}
		if (name.equals("c")) {
			// Print the cell reference
			String rValue = attributes.getValue("r");
			colChar = new String(new char[] { rValue.charAt(0) });
			if (!isFirstRow) {
				columnName = headerMap.get(colChar);
			}
			LOGGER.debug("rValue : " + rValue + ", rValue.charAt(0): "
					+ rValue.charAt(0) + ", columnName: " + columnName);
			// Figure out if the value is an index in the SST
			String cellType = attributes.getValue("t");
			if (cellType != null && cellType.equals("s")) {
				nextIsString = true;
			} else {
				nextIsString = false;
			}
		}
		// Clear contents cache
		lastContents = "";
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// Process the last contents as required.
		// Do now, as characters() may be called more than once
		if (nextIsString) {
			int idx = Integer.parseInt(lastContents);
			lastContents = new XSSFRichTextString(sst.getEntryAt(idx))
					.toString();
			nextIsString = false;
		}

		// v => contents of a cell Output after we've seen the string contents
		if (name.equals("v")) {
			if (isFirstRow) {
				headerMap.put(colChar, lastContents.trim().replace(" ", "")
						.toLowerCase());
			}
			if (!isFirstRow && columnName != null) {
				data.put(columnName, lastContents);
			}
		}
		if (name.equals("row") && !isFirstRow) {
			persistModel(data, rowNo);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		lastContents += new String(ch, start, length);
	}

	public void validateModel(AbstractXMLDataModel model)
			throws VedantuException {
	}

	public void persistModel(Map<String, String> data, int rowNo) {
		LOGGER.debug("data collected: " + data + ", rowNo : " + rowNo);
	}

}
