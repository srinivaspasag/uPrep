package com.vedantu.ext.cmds.db.datamanagers;

import com.vedantu.ext.cmds.db.ContentValues;
import com.vedantu.ext.cmds.db.SQLiteDataHelper;
import com.vedantu.ext.cmds.db.models.AbstractDBModel;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.db.FieldInfo;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractDataManager<T extends AbstractDBModel> {

    public static final String    DELETE_FROM     = "delete from";
    protected static final String SPACE           = " ";
    protected static final String KEYWORD_AND     = " AND ";
    @SuppressWarnings("unused")
    protected static final String KEYWORD_OR      = " OR ";
    protected static final String KEYWORD_FROM    = " FROM ";
    protected static final String KEYWORD_WHERE   = " WHERE ";
    protected static final String KEYWORD_GROUPBY = " GROUP BY ";
    protected static final String KEYWORD_IN      = " IN ";

    protected SQLiteDataHelper    dbHelper;

    protected Logger              LOGGER;

    protected Class<T>            entityClazz;

    public AbstractDataManager(Class<T> entityClazz) {

        super();
        this.entityClazz = entityClazz;
        dbHelper = SQLiteDataHelper.INSTANCE;
        LOGGER = Logger.getLogger(getClass());
    }

    public int insert(T model) throws Exception {

        LOGGER.debug("inserting into table : " + getTableName());
        ContentValues values = model.toContentValues();
        model._id = insert(getTableName(), values);
        return model._id;
    }
    public int insertIfDoesntExist(T model) throws Exception {

        LOGGER.debug("inserting into table : " + getTableName());
        ContentValues values = model.toContentValues();
        LOGGER.debug("getting content values into table : " + getTableName() + dbHelper);
        ensureDBHelper();
        model._id = dbHelper.insertWithOnConflict(getTableName(), null, values,SQLiteDataHelper.CONFLICT_IGNORE);
        return model._id;
    }

    public int delete(FieldInfo... fields) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append(DELETE_FROM).append(" ").append(getTableName()).append(" ");
        createWhereFields(sb, KEYWORD_AND, fields);
        return rawUpdate(sb.toString());
    }

    public T upsert(T model) throws Exception {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, new String[] { ConstantGlobal._ID, ConstantGlobal.TIME_CREATED });
        sb.append(" WHERE ");
        sb.append(getUpsertQueryCondition(model));
        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                int _id = rs.getInt(1);
                if (_id >= 0) {
                    model._id = _id;
                    model.timeCreated = Long.valueOf(rs.getString(2));
                    update(model);
                }
            } else {
                model._id = insert(model);
            }
        } finally {
            closeResultSet(rs);
        }
        return model;
    }

    // public int update(T model) {
    //
    // return update(model);
    // }

    public int update(T model, String... fields) {

        ContentValues values = null;
        try {

            values = model.toContentValues();
            if (fields != null && fields.length != 0) {
                Set<String> fieldsInput = new HashSet<String>();

                Collections.addAll(fieldsInput, fields);
                Set<String> keysForRemoval = new HashSet<String>(values.keySet());
                for (String key : keysForRemoval) {
                    if (!fieldsInput.contains(key)) {
                        values.remove(key);
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(getTableName(), values, "_id=" + model._id);
    }

    private void ensureDBHelper() {

        if (dbHelper == null) {
            dbHelper = SQLiteDataHelper.INSTANCE;
        }
    }

    protected abstract String getTableName();

    protected abstract String getUpsertQueryCondition(T model);

    protected void addSelectQuery(StringBuilder sb, String[] fields) {

        addSelectQuery(sb, fields, null);
    }

    protected void addSelectQuery(StringBuilder sb, String[] fields, String customFieldStatement) {

        sb.append("SELECT ");
        if (fields == null || fields.length == 0) {
            sb.append("*");
        } else {
            boolean start = true;
            for (String f : fields) {
                if (!start) {
                    sb.append(",");
                }
                sb.append(f);
                start = false;
            }
            if (!StringUtils.isEmpty(customFieldStatement)) {
                sb.append(",").append(customFieldStatement);
            }
        }
        sb.append(" FROM ").append(getTableName()).append(" ");
    }

    protected void addLimitFilter(StringBuilder sb, String orderBy, String sortOrder, int start,
            int size) {

        if (!StringUtils.isEmpty(orderBy)) {
            if (orderBy.equals(ConstantGlobal.NAME)) {
                orderBy = "LOWER(" + orderBy + ")";
            }
            sb.append("ORDER BY ").append(orderBy).append(" ");
        }
        if (!StringUtils.isEmpty(sortOrder)) {
            sb.append(sortOrder).append(" ");
        }

        if (size != SQLDBUtils.NO_LIMIT) {
            sb.append("LIMIT ").append(start).append(",").append(size).append(" ");
        }
    }

    protected static void addCreateTableQuery(StringBuilder sb, String tableName,
            FieldInfo... fields) {

        sb.append("create table if not exists ").append(tableName).append(" (");

        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                sb.append(fields[i].toString());
                if (i < (fields.length - 1)) {
                    sb.append(",");
                }
            }
            sb.append(")");
        }

    }

    protected static void createUpdateSetQuery(StringBuilder sb, String tableName,
            com.vedantu.ext.cmds.utils.db.FieldInfo... fields) {

        sb.append("update ").append(tableName).append(" SET ");

        List<String> fieldValues = new ArrayList<String>();

        for (com.vedantu.ext.cmds.utils.db.FieldInfo field : fields) {
            fieldValues.add(field.toString());
        }
        sb.append(StringUtils.join(" , ", fieldValues));
        sb.append(" ");
    }

    protected static void createWhereFields(StringBuilder sb, String booleanOp,
            com.vedantu.ext.cmds.utils.db.FieldInfo... fields) {

        sb.append(KEYWORD_WHERE);

        List<String> fieldValues = new ArrayList<String>();

        for (com.vedantu.ext.cmds.utils.db.FieldInfo field : fields) {
            StringBuilder equalityBuilder = new StringBuilder();
            if (field.values != null) {

                equalityBuilder.append(field.field);
                equalityBuilder.append(KEYWORD_IN);
                if( field.value instanceof String ){
                    equalityBuilder.append("'");
                    equalityBuilder.append(field.value);
                    equalityBuilder.append("'");
                    
                }else{
                    equalityBuilder.append(field.value);
             
                }
       
            } else {
                equalityBuilder.append(field.field);
                equalityBuilder.append("=");
                if( field.value instanceof String ){
                    equalityBuilder.append("'");
                    equalityBuilder.append(field.value);
                    equalityBuilder.append("'");
                    
                }else{
                    equalityBuilder.append(field.value);
             
                }

            }
            fieldValues.add(equalityBuilder.toString());
        }

        sb.append(StringUtils.join(booleanOp, fieldValues));
        sb.append(" ");
    }

    protected static void createGroupBy(StringBuilder sb, String... fields) {

        sb.append(KEYWORD_GROUPBY);

        sb.append(StringUtils.join(",", fields));
        sb.append(" ");
    }

    protected static void addCreateTableQuery(StringBuilder sb, String tableName) {

        sb.append("create table if not exists ").append(tableName).append(" (");

    }

    protected static void endCreateTableQuery(StringBuilder sb) {

        sb.append(");");
    }

    protected boolean addIntEqualSQLQuery(String fieldName, int value, StringBuilder sb) {

        sb.append(fieldName).append("=").append(value).append(SPACE);
        return true;
    }

    @SuppressWarnings("unused")
    protected boolean addIntNotEqualSQLQuery(String fieldName, int value, StringBuilder sb) {

        sb.append(fieldName).append("!=").append(value).append(SPACE);
        return true;
    }

    @SuppressWarnings("unused")
    protected boolean addBooleanEqualSQLQuery(String fieldName, boolean value, StringBuilder sb) {

        sb.append(fieldName).append("=").append(value ? 1 : 0).append(SPACE);
        return true;
    }

    protected boolean addStringEqualSQLQuery(String fieldName, String value, StringBuilder sb,
            boolean ignoreCase) {

        if (value == null) {
            return false;
        }
        if (ignoreCase) {
            sb.append("LOWER(").append(fieldName).append(")").append("='")
                    .append(value.toLowerCase()).append("' ");
        } else {
            sb.append(fieldName).append("='").append(value.trim()).append("' ");
        }
        return true;
    }

    protected static void addAbstractAbstractDataModelFeildsRow(StringBuilder sb) {

        sb.append(ConstantGlobal._ID).append(" integer primary key autoincrement,");
        sb.append(ConstantGlobal.ORG_KEY_ID).append(" integer,");
        sb.append(ConstantGlobal.TIME_CREATED).append(" unsigned big int not null,");
    }

    protected static String createIndexQuery(String tableName, String indexName, boolean unique,
            String... fields) {

        StringBuilder sb = new StringBuilder();
        sb.append("create ");
        if (unique) {
            sb.append("unique ");
        }
        sb.append("index if not exists ").append(indexName);
        sb.append(" on ").append(tableName);
        sb.append("(").append(StringUtils.join(",", fields)).append(")");
        return sb.toString();
    }

    protected int insert(String table, ContentValues values) {

        LOGGER.debug("inserting values: " + values + ", to : " + table);
        ensureDBHelper();
        return dbHelper.insert(table, null, values);
    }

    protected int update(String table, ContentValues values, String whereClause) {

        LOGGER.debug("updating table:" + table + ", values: " + values + ", whereClause: "
                + whereClause);
        ensureDBHelper();
        return dbHelper.update(table, values, whereClause);
    }

    protected ResultSet rawQuery(String sql) {

        try {
            LOGGER.debug("query: " + sql);
            ensureDBHelper();
            return dbHelper.rawQuery(sql);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    protected ResultSet rawQuery(String sql, List<Object> values) {

        try {
            LOGGER.debug("query: " + sql);
            ensureDBHelper();
            return dbHelper.rawQuery(sql, values);
        } catch (SQLException e) {
            LOGGER.fatal(" Unable to execute query");
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    protected ResultSet rawQueryUnprepared(String sql) {

        try {
            LOGGER.debug("unprepared query: " + sql);
            ensureDBHelper();
            return dbHelper.rawQueryUnprepared(sql);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    protected int rawUpdate(String sql) {

        try {
            LOGGER.debug("query: " + sql);
            ensureDBHelper();
            return dbHelper.rawUpdateQuery(sql);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return 0;
        }
    }

    protected void closeResultSet(ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
