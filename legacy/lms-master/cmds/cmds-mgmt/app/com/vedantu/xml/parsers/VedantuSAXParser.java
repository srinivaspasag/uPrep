package com.vedantu.xml.parsers;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

public class VedantuSAXParser extends SAXParser {

	public void parse(InputSource inputSource) throws SAXException, IOException {
		super.parse(inputSource);
	}

}
