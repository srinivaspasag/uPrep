package com.vedantu.ext.cmds.db;

import com.vedantu.ext.cmds.enums.SQLLITEDataType;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class FieldInfo {

    private String          name;
    private SQLLITEDataType type;
    private boolean         isNull        = true;
    private boolean         isPrimary     = false;
    private boolean         autoIncrement = false;

    public FieldInfo(String name, SQLLITEDataType type, boolean isNull, boolean isPrimary) {

        this.name = name;
        this.type = type;
        this.isNull = isNull;
        this.isPrimary = isPrimary;
    }

    public FieldInfo(String name, SQLLITEDataType type, boolean isNull, boolean isPrimary,
            boolean isAutoIncrement) {

        this(name, type, isNull, isPrimary);
        if (type == SQLLITEDataType.BIG_INT || type == SQLLITEDataType.DOUBLE) {
            this.autoIncrement = isAutoIncrement;
        }

    }

    @Override
    public String toString() {

        return name + " " + type + " " + ((isNull) ? StringUtils.EMPTY : SQLDBUtils.NOT_NULL + " ")
                + ((isPrimary) ? "primary key " : StringUtils.EMPTY)
                + ((autoIncrement) ? " autoincrement " : StringUtils.EMPTY);
    }
}
