package com.vedantu.ext.cmds.enums;

public enum SQLLITEDataType {

    /**
     * This will be updated as we will use more and more data types
     */

    // Integers
    // INT
    // INTEGER
    // TINYINT
    // SMALLINT
    // MEDIUMINT
    // BIGINT
    // UNSIGNED BIG INT
    // INT2
    // INT8
    BIG_INT("BIGINT"),
    UNSIGNED_BIG_INT("UNSIGNED BIG INT"),
    INTEGER("INTEGER"),

    // REAL
    // DOUBLE
    // DOUBLE PRECISION
    // FLOAT
    DOUBLE("DOUBLE"),
    //
    // CHARACTER(20)
    // VARCHAR(255)
    // VARYING CHARACTER(255)
    // NCHAR(55)
    // NATIVE CHARACTER(70)
    // NVARCHAR(100)
    // TEXT
    // CLOB

    TEXT("TEXT"), ;

    private String typeName = "";

    private SQLLITEDataType(String value) {

        this.typeName = value;
    }

    @Override
    public String toString() {

        return typeName;
    }

}
