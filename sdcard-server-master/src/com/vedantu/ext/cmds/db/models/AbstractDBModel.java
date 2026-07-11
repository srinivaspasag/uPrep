package com.vedantu.ext.cmds.db.models;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import com.vedantu.ext.cmds.db.ContentValues;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;

public class AbstractDBModel implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public int                _id;
    public int                orgKeyId;
    public long               timeCreated;

    public AbstractDBModel() {

        this(0);
    }

    public AbstractDBModel(int orgKeyId) {

        super();
        this.orgKeyId = orgKeyId;
        this.timeCreated = System.currentTimeMillis();
    }

    public ContentValues toContentValues() throws Exception {

        ContentValues values = new ContentValues();
        Class<?> clazz = this.getClass();
        for (Field f : clazz.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            Class<?> fClass = f.getType();

            if (fClass.isPrimitive()) {
                if (int.class.equals(fClass)) {
                    values.put(f.getName(), f.getInt(this));
                } else if (long.class.equals(fClass)) {
                    values.put(f.getName(), String.valueOf(f.getLong(this)));
                } else if (double.class.equals(fClass)) {
                    values.put(f.getName(), f.getDouble(this));
                } else if (float.class.equals(fClass)) {
                    values.put(f.getName(), f.getFloat(this));
                } else if (boolean.class.equals(fClass)) {
                    values.put(f.getName(), f.getBoolean(this) ? 1 : 0);
                }
            } else if (String.class.equals(fClass)) {
                values.put(f.getName(), (String) f.get(this));
            } else if (byte[].class.equals(fClass)) {
                values.put(f.getName(), (byte[]) f.get(this));
            } else if (JSONObject.class.equals(fClass)) {
                values.put(f.getName(), f.get(this).toString());
            } else if (JSONArray.class.equals(fClass)) {
                values.put(f.getName(), f.get(this).toString());
            } else if(Collection.class.isAssignableFrom(f.getType())) {
                List<String> val =  (ArrayList<String>)(f.get(this)) ;
                String s_value = "";
                for (String s : val)
                {
                    s_value += s + ",";
                }
                values.put(f.getName(),s_value);
            }
        }
        values.remove(ConstantGlobal._ID);
        return values;
    }

    public int get_id() {

        return _id;
    }

    public int getOrgKeyId() {

        return orgKeyId;
    }

    public long getTimeCreated() {

        return timeCreated;
    }

}
