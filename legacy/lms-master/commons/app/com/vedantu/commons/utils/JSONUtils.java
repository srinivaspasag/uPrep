package com.vedantu.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.DBObject;
import com.vedantu.commons.events.apis.JSONAware;

public class JSONUtils {

    private static final ALogger LOGGER = Logger.of(JSONUtils.class);

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
                LOGGER.error(e.getMessage(), e);
            }
        }
        return resultMap;
    }

    public static Collection<? extends JSONAware> getJSONAwareCollection(Class<?> jAware,
            JSONObject json, String key) {

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
            LOGGER.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return jsonAwareCollection;
    }

    public static JSONAware getJSONAware(JSONAware jAware, JSONArray jsonArray, int index) {

        try {
            if (jAware != null) {
                jAware.fromJSON(jsonArray.getJSONObject(index));
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return jAware;
    }

    public static JSONAware getJSONAware(JSONAware jAware, JSONObject json, String key) {

        if (jAware != null) {
            JSONObject jsonObject = getJSONObject(json, key);
            if (jsonObject != null && jsonObject.length() > 0) {
                jAware.fromJSON(jsonObject);
            }
        }

        return jAware;
    }

    public static boolean getBoolean(JSONObject json, String key) {

        return getBoolean(json, key, false);
    }

    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {

        boolean value = defaultValue;
        try {
            value = json.getBoolean(key);
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
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
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
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
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
        }
        return value;
    }

    public static double getDouble(JSONObject json, String key) {

        return getDouble(json, key, 0.0);
    }

    public static double getDouble(JSONObject json, String key, double defaultValue) {

        double value = defaultValue;
        try {
            value = json.getDouble(key);
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
        }
        return value;
    }

    public static String getString(JSONObject json, String key) {

        return getString(json, key, StringUtils.EMPTY);
    }

    public static String getString(JSONObject json, String key, String defaultValue) {

        String value = defaultValue;
        if (json == null) {
            return value;
        }
        try {
            value = json.getString(key);
            if (value == null) {
                value = StringUtils.EMPTY;
            }
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
        }
        return value;
    }

    public static JSONArray getJSONArray(JSONObject json, String key) {

        return getJSONArray(json, key, new JSONArray());
    }

    public static JSONArray getJSONArray(JSONObject json, String key, JSONArray defaultValue) {

        JSONArray value = defaultValue;
        try {
            if (json.has(key)) {
                value = json.getJSONArray(key);
                if (value == null) {
                    value = new JSONArray();
                }
            }
        } catch (JSONException e) {
            LOGGER.error("missing key : " + key + " in json : " + json);
        }
        return value;
    }

    public static List<String> getList(JSONObject json, String key) {

        return getList(json, key, new ArrayList<String>());
    }

    public static List<String> getList(JSONObject json, String key, List<String> defaultValue) {

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
                    LOGGER.error("missing index : " + i + " in jsonarray : " + a, e);
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
                    LOGGER.error("missing index : " + i + " in jsonarray : " + a, e);
                }
            }
        }
        return value;
    }

    public static Set<String> getSet(JSONObject json, String key) {

        return getSet(json, key, new HashSet<String>());
    }

    public static Set<String> getSet(JSONObject json, String key, Set<String> defaultValue) {

        Set<String> value = defaultValue;
        JSONArray a = getJSONArray(json, key);
        if (null != a) {
            if (null == value) {
                value = new HashSet<String>();
            }
            for (int i = 0; i < a.length(); i++) {
                try {
                    value.add(a.getString(i));
                } catch (JSONException e) {
                    LOGGER.error("missing index : " + i + " in jsonarray : " + a, e);
                }
            }
        }
        return value;
    }

    public static JSONObject getJSONObject(JSONObject json, String key) {

        return getJSONObject(json, key, new JSONObject());
    }

    public static JSONObject getJSONObject(JSONObject json, String key, JSONObject defaultValue) {

        JSONObject value = defaultValue;
        try {
            value = json.getJSONObject(key);
            if (value == null) {
                value = new JSONObject();
            }
        } catch (JSONException e) {
            LOGGER.error("missing key: " + key + " in jsonObject : " + json, e);
        }
        return value;
    }

    public static Object getObject(JSONObject json, String key) {

        return getObject(json, key, new Object());
    }

    public static Object getObject(JSONObject json, String key, Object defaultValue) {

        Object value = defaultValue;
        try {
            value = json.get(key);
            if (value == null) {
                value = new JSONObject();
            }
        } catch (JSONException e) {
            LOGGER.error("missing key: " + key + " in jsonObject : " + json, e);
        }
        return value;
    }

    public static JSONObject getJSONObject(DBObject dbObject) {

        JSONObject json = new JSONObject();
        if (dbObject != null) {
            for (String key : dbObject.keySet()) {
                try {
                    json.put(key, dbObject.get(key));
                } catch (JSONException e) {
                    LOGGER.error("error on putting key [ " + key + " ] to json object");
                }

            }
        }
        return json;
    }

    public static List<JSONObject> getJSONObject(List<DBObject> dbObjectList) {

        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (DBObject dbObject : dbObjectList) {
            jsonList.add(getJSONObject(dbObject));
        }
        return jsonList;
    }

    public static void ensureStringKey(JSONObject json, String key) {

        if (!json.has(key)) {
            try {
                json.put(key, StringUtils.EMPTY);
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void ensureIntKey(JSONObject json, String key) {

        if (!json.has(key)) {
            try {
                json.put(key, 0);
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void addJSONAwareObjectList(String key,
            Collection<? extends JSONAware> jsonAwareObjects, JSONObject json) throws JSONException {

        if (json != null && jsonAwareObjects != null && !jsonAwareObjects.isEmpty()) {
            JSONArray jArray = new JSONArray();
            for (JSONAware jsonAware : jsonAwareObjects) {
                jArray.put(jsonAware.toJSON());
            }
            json.put(key, jArray);
        }
    }

    public static void addJSONAwareObject(String key, JSONAware jsonAware, JSONObject json)
            throws JSONException {

        if (json != null) {
            if (jsonAware != null) {
                json.put(key, jsonAware.toJSON());
            } else {
                json.put(key, new JSONObject());
            }
        }
    }

    public static void addStringCollection(String key, Collection<String> values, JSONObject json)
            throws JSONException {

        if (CollectionUtils.isNotEmpty(values)) {
            json.put(key, values);
        } else {
            json.put(key, new HashSet<String>());
        }
    }

    public static void removeKeys(JSONObject superJson, JSONObject childJson) {

        @SuppressWarnings("unchecked")
        Iterator<String> keys = childJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (superJson.has(key) && !superJson.isNull(key)) {
                superJson.remove(key);
            }
        }
    }
}
