package com.vedantu.xml.parsers;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import play.Logger;
import play.Logger.ALogger;

public class WorkBookDataHandaler extends DefaultHandler {

	private static final ALogger LOGGER = Logger.of(WorkBookDataHandaler.class);
	private String lastContents;
	private List<String> sheetNames;

	public WorkBookDataHandaler(List<String> sheetNames) {
		super();
		this.sheetNames = sheetNames;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		// c => cell
		LOGGER.info("name of element: " + name);
		if (name.equals("sheet")) {
			// Print the cell reference
			String cellValue = attributes.getValue("name");

			if (cellValue != null) {
				lastContents = cellValue;
			}
		} else {
			// Clear contents cache
			lastContents = "";
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (name.equals("sheet")) {
			LOGGER.info("sheetName: " + lastContents);
			sheetNames.add(lastContents);

		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		lastContents += new String(ch, start, length);
	}

}
