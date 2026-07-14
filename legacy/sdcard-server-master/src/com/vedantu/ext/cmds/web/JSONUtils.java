package com.vedantu.ext.cmds.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

	private static final Logger LOGGER = Logger.getLogger("JSONUtils");

	public static Map<String, Object> toMap(JSONObject json) {

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		if (json == null) {
			return resultMap;
		}
		@SuppressWarnings("unchecked")
		Iterator<String> it = json.keys();
		while (it.hasNext()) {
			String key = it.next();
			try {
				resultMap.put(key, json.get(key));
			} catch (JSONException e) {
				LOGGER.info(e.getMessage());
			}
		}
		return resultMap;
	}

	public static String getString(JSONObject json, String key) {

		String value = "";
		try {
			if (json != null) {
				value = json.getString(key);
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static JSONObject getJSONObject(JSONObject json, String key) {

		return getJSONObject(json, key, new JSONObject());
	}

	public static JSONObject getJSONObject(JSONObject json, String key,
			JSONObject defaultValue) {

		JSONObject value = defaultValue;
		try {
			value = json.getJSONObject(key);
			if (value == null) {
				value = new JSONObject();
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static Collection<? extends JSONAware> getJSONAwareCollection(
			Class<?> jAware, JSONObject json, String key) {

		Collection<JSONAware> jsonAwareCollection = new ArrayList<JSONAware>();
		try {
			JSONArray jArray = JSONUtils.getJSONArray(json, key);
			if (jArray != null) {
				for (int i = 0; i < jArray.length(); i++) {
					JSONAware jA = (JSONAware) jAware.newInstance();
					jA.fromJSON(jArray.getJSONObject(i));
					jsonAwareCollection.add(jA);
				}
			}
		} catch (JSONException e) {
			LOGGER.info(e.getMessage());
		} catch (InstantiationException e) {
			LOGGER.info(e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.info(e.getMessage());
		}
		return jsonAwareCollection;
	}

	public static JSONArray getJSONArray(JSONObject json, String key) {

		return getJSONArray(json, key, new JSONArray());
	}

	public static JSONArray getJSONArray(JSONObject json, String key,
			JSONArray defaultValue) {

		JSONArray value = defaultValue;
		try {
			if (json.has(key)) {
				value = json.getJSONArray(key);
				if (value == null) {
					value = new JSONArray();
				}
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static int getInt(JSONObject json, String key) {

		return getInt(json, key, 0);
	}

	public static int getInt(JSONObject json, String key, int defaultValue) {

		int value = defaultValue;
		try {
			value = json.getInt(key);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static long getLong(JSONObject json, String key) {

		return getLong(json, key, 0);
	}

	public static long getLong(JSONObject json, String key, long defaultValue) {

		long value = defaultValue;
		try {
			value = json.getLong(key);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static boolean getBoolean(JSONObject json, String key) {

		return getBoolean(json, key, false);
	}

	public static boolean getBoolean(JSONObject json, String key,
			boolean defaultValue) {

		boolean value = defaultValue;
		try {
			value = json.getBoolean(key);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		return value;
	}

	public static List<String> getList(JSONObject json, String key) {

		return getList(json, key, new ArrayList<String>());
	}

	public static List<String> getList(JSONObject json, String key,
			List<String> defaultValue) {

		List<String> value = defaultValue;
		JSONArray a = getJSONArray(json, key);
		if (null != a) {
			if (null == value) {
				value = new ArrayList<String>();
			}
			for (int i = 0; i < a.length(); i++) {
				try {
					value.add(a.getString(i));
				} catch (JSONException e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
		return value;
	}

	public static List<Integer> getIntegerList(JSONObject json, String key) {

		return getIntegerList(json, key, new ArrayList<Integer>());
	}

	public static List<Integer> getIntegerList(JSONObject json, String key,
			List<Integer> defaultValue) {

		List<Integer> value = defaultValue;
		JSONArray a = getJSONArray(json, key);
		if (null != a) {
			if (null == value) {
				value = new ArrayList<Integer>();
			}
			for (int i = 0; i < a.length(); i++) {
				try {
					value.add(a.getInt(i));
				} catch (JSONException e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
		return value;
	}

	public static List<JSONObject> arrayToList(JSONArray array) {

		List<JSONObject> list = new ArrayList<JSONObject>();
		for (int i = 0; i < array.length(); i++) {
			try {
				list.add(array.getJSONObject(i));
			} catch (JSONException e) {
				LOGGER.info(e.getMessage());
			}
		}
		return list;
	}

	public static boolean isEmpty(JSONArray jArray) {

		return jArray == null || jArray.length() == 0;
	}

	public static JSONArray concatArray(JSONArray... arrs) throws JSONException {

		JSONArray result = new JSONArray();
		for (JSONArray arr : arrs) {
			for (int i = 0; i < arr.length(); i++) {
				result.put(arr.get(i));
			}
		}
		return result;
	}

	public static void putValue(String key, Object value, JSONObject json) {

		try {
			json.put(key, value);
		} catch (JSONException e) {
		}
	}

}
