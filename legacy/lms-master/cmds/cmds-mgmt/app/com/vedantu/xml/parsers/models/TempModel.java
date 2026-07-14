package com.vedantu.xml.parsers.models;

public class TempModel extends AbstractXMLDataModel {

	public String key;
	public String value;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{key:").append(key).append(", value:").append(value)
				.append(", uuid:").append(uuid).append("}");
		return builder.toString();
	}

	

}
