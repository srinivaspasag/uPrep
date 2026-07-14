package com.vedantu.ext.cmds.utils.db;

import java.util.List;

public class FieldInfo {

    public String       field;

    public Object       value  = null;
    public List<String> values = null;

    public FieldInfo(String field, Object value) {

        super();
        this.field = field;
        this.value = value;
    }

    public FieldInfo(String field, List<String> values) {

        super();
        this.field = field;
        this.values = values;
    }

    public void clear() {

        field = null;
        value = null;

    }

    @Override
    public String toString() {

        return field + "=" + value;
    }
}