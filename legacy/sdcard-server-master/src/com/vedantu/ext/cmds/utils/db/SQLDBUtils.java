
package com.vedantu.ext.cmds.utils.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.ext.cmds.utils.commons.StringUtils;

public class SQLDBUtils {

    public static final int    NO_START   = 0;
    public static final int    NO_LIMIT   = 0;
    public static final String ORDER_ASC  = "ASC";
    public static final String ORDER_DESC = "DESC";

    public static final String NOT_NULL   = "not null";

    private static Logger      LOGGER     = Logger.getLogger("SQLDBUtils");

    public static <T> T convertToValues(ResultSet rs, Class<T> clazz, String[] fields) {

        return convertToValues(rs, clazz, fields, null);
    }

    public static <T> T convertToValues(ResultSet rs, Class<T> clazz, String[] fields,
            Set<String> cColumnNames) {

        T object = null;
        try {
            object = clazz.newInstance();
            if (fields == null || fields.length == 0) {
                for (Field f : clazz.getFields()) {
                    setValue(f, rs, object, cColumnNames);
                    // f.set(object, getObject(cursor, f.getName()));
                }
            } else {
                for (String field : fields) {
                    Field f = clazz.getField(field);
                    if (f != null) {
                        setValue(f, rs, object, cColumnNames);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return object;
    }

    private static <T> void setValue(Field f, ResultSet rs, T object, Set<String> cColumnName)
            throws IllegalArgumentException, IllegalAccessException, SQLException {

        if (Modifier.isStatic(f.getModifiers())
                || (cColumnName != null && !cColumnName.contains(f.getName()))) {
            return;
        }
        // Log.d(TAG, "fieldName:" + f.getName());

        // Object value = getObject(cursor, f.getName());

        if (String.class.equals(f.getType())) {
            f.set(object, rs.getString(f.getName()));
        } else if (int.class.equals(f.getType())) {
            f.set(object, rs.getInt(f.getName()));
        } else if (JSONObject.class.equals(f.getType())) {
            String val = rs.getString(f.getName());
            try {
                f.set(object, new JSONObject(val));
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else if (JSONArray.class.equals(f.getType())) {
            String val = rs.getString(f.getName());
            try {
                f.set(object, new JSONArray(val));
            } catch (JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else if (boolean.class.equals(f.getType())) {
            int val = rs.getInt(f.getName());
            f.set(object, val > 0 ? true : false);
        } else if (double.class.equals(f.getType())) {
            f.set(object, rs.getDouble(f.getName()));
        } else if (long.class.equals(f.getType())) {
            f.set(object, rs.getLong(f.getName()));
        } else if (float.class.equals(f.getType())) {
            f.set(object, rs.getFloat(f.getName()));
        } else if (byte[].class.equals(f.getType())) {
            f.set(object, rs.getBlob(f.getName()));
        } else if(Collection.class.isAssignableFrom(f.getType())) {
             List<String> val =new ArrayList<String>(Arrays.asList((rs.getString(f.getName())).split(",")));
             LOGGER.debug("List of values in resultset is :"+rs.getString(f.getName()));
            f.set(object, val);
        }else {
            f.set(object, rs.getString(f.getName()));
        }
    }
    

    public static String prepareStringsForInQuery(String listQueryType,List<String> inValues) {

        List<String> targets = new ArrayList<String>();
        for (String targetId : inValues) {

            targets.add("?");
        }
        String returnValues = StringUtils.join(",", targets);
        return listQueryType + "(" + returnValues + ")";
    }


}
