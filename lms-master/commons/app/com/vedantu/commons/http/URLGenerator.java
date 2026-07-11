package com.vedantu.commons.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class URLGenerator {
	public static final String	PARAM_STRING_SEPARATOR		= "&";
	public static final String	PARAM_EQUAL					= "=";
	public static final String	ENDPOINT_PARAM_SEPARATOR	= "?";
	public static final String	PROTOCOL_HOST_SEPARATOR		= "://";
	public static final String	HOST_PORT_SEPARATOR_COLON	= ":";
	public static final String	FORWARD_SLASH				= "/";
	public String				host;
	public int					port						= -1;
	public String				endPoint;
	public Map<String, Object>	params;
	public String				protocol;

	public String generate() {

		StringBuffer urlBuffer = new StringBuffer();
		urlBuffer.append(protocol);
		urlBuffer.append(PROTOCOL_HOST_SEPARATOR);
		urlBuffer.append(host);
		if (port != -1) {
			urlBuffer.append(HOST_PORT_SEPARATOR_COLON);
			urlBuffer.append(port);
		}
		urlBuffer.append(FORWARD_SLASH);
		urlBuffer.append(endPoint);

		urlBuffer.append(ENDPOINT_PARAM_SEPARATOR);
		StringBuffer keyValue = null;
		List<String> keyValues = new ArrayList<String>();
		for (String key : params.keySet()) {
			keyValue = new StringBuffer();
			keyValue.append(key);
			keyValue.append(PARAM_EQUAL);
			keyValue.append(params.get(key).toString());
			keyValues.add(keyValue.toString());
		}

		String params = StringUtils.join(keyValues.toArray(),
				PARAM_STRING_SEPARATOR);
		urlBuffer.append(params);
		// TODO check with regex
		return urlBuffer.toString();
	}
}
