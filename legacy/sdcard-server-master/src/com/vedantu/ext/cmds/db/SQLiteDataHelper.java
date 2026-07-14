package com.vedantu.ext.cmds.db;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vedantu.ext.cmds.db.datamanagers.FileDownloadInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.FlashRecordDataManager;
import com.vedantu.ext.cmds.db.datamanagers.FolderDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ImportedLibraryDataManager;
import com.vedantu.ext.cmds.db.datamanagers.JobInfoDataManager;
import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SDCardDataManger;
import com.vedantu.ext.cmds.db.datamanagers.SDCardGroupDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SyncInfoDataManager;
import com.vedantu.ext.cmds.utils.commons.StringUtils;

public class SQLiteDataHelper {

    public static SQLiteDataHelper INSTANCE          = new SQLiteDataHelper();

    private static final String    DB_NAME           = "vedantu";
    private Logger                 LOGGER;
    private Connection             conn;
    // private Context ctx;

    public static final int        CONFLICT_ROLLBACK = 1;

    /**
     * When a constraint violation occurs,no ROLLBACK is executed so changes from prior commands
     * within the same transaction are preserved. This is the default behavior.
     */
    public static final int        CONFLICT_ABORT    = 2;

    /**
     * When a constraint violation occurs, the command aborts with a return code SQLITE_CONSTRAINT.
     * But any changes to the database that the command made prior to encountering the constraint
     * violation are preserved and are not backed out.
     */
    public static final int        CONFLICT_FAIL     = 3;

    /**
     * When a constraint violation occurs, the one row that contains the constraint violation is not
     * inserted or changed. But the command continues executing normally. Other rows before and
     * after the row that contained the constraint violation continue to be inserted or updated
     * normally. No error is returned.
     */
    public static final int        CONFLICT_IGNORE   = 4;

    /**
     * When a UNIQUE constraint violation occurs, the pre-existing rows that are causing the
     * constraint violation are removed prior to inserting or updating the current row. Thus the
     * insert or update always occurs. The command continues executing normally. No error is
     * returned. If a NOT NULL constraint violation occurs, the NULL value is replaced by the
     * default value for that column. If the column has no default value, then the ABORT algorithm
     * is used. If a CHECK constraint violation occurs then the IGNORE algorithm is used. When this
     * conflict resolution strategy deletes rows in order to satisfy a constraint, it does not
     * invoke delete triggers on those rows. This behavior might change in a future release.
     */
    public static final int        CONFLICT_REPLACE  = 5;

    /**
     * Use the following when no conflict action is specified.
     */
    public static final int        CONFLICT_NONE     = 0;

    private static final String[]  CONFLICT_VALUES   = new String[] { "", " OR ROLLBACK ",
            " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE " };

    private SQLiteDataHelper() {

        super();
        LOGGER = Logger.getLogger(getClass().getSimpleName());
        init();
    }

    private void init() {

        LOGGER.info("configuring and getting db connection");
        try {

            String sDriverName = "org.sqlite.JDBC";
            Class.forName(sDriverName);

            File f = new File(System.getProperty("user.home") + "/.vedantu-dbs");
            synchronized (f) {
                if (!f.exists()) {
                    f.mkdirs();
                }
            }

            String sDbUrl = "jdbc:sqlite" + ":" + f.getAbsolutePath() + File.separator + DB_NAME
                    + ".db";

            this.conn = DriverManager.getConnection(sDbUrl);
            // createTablesBatched();
            createTablesIterative();
        } catch (SQLException se) {
            LOGGER.error(se.getMessage(), se);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public Connection getConnection() {

        return conn;
    }

    public void closeConnection() {

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void createTablesBatched() throws SQLException {

        Statement stat = conn.createStatement();
        List<String> tabelQueries = getCreateTablesQuery();
        if (tabelQueries.isEmpty()) {
            return;
        }
        for (String tableQuery : tabelQueries) {
            stat.addBatch(tableQuery);
        }
        stat.executeBatch();
        stat.clearBatch();
        stat.close();
    }

    private void createTablesIterative() throws SQLException {

        Statement stat = conn.createStatement();
        List<String> tabelQueries = getCreateTablesQuery();
        if (tabelQueries.isEmpty()) {
            return;
        }
        for (String tableQuery : tabelQueries) {
            try {
                LOGGER.debug("SQL QUERY :" + tableQuery);
                stat.execute(tableQuery);
            } catch (SQLException ex) {
                LOGGER.error("SQL Exception ", ex);
            }

        }

        stat.close();
    }

    public int insert(String table, String nullColumnHack, ContentValues values) {

        try {
            return insertWithOnConflict(table, nullColumnHack, values, CONFLICT_NONE);
        } catch (SQLException e) {
            LOGGER.error("Error inserting " + values, e);
            return -1;
        }
    }

    public int insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException {

        return insertWithOnConflict(table, nullColumnHack, values, CONFLICT_NONE);
    }

    public int replace(String table, String nullColumnHack, ContentValues initialValues)
            throws SQLException {

        try {
            return insertWithOnConflict(table, nullColumnHack, initialValues, CONFLICT_REPLACE);
        } catch (SQLException e) {
            LOGGER.error("Error inserting " + initialValues, e);
            return -1;
        }
    }

    public int replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues)
            throws SQLException {

        return insertWithOnConflict(table, nullColumnHack, initialValues, CONFLICT_REPLACE);
    }

    public int insertWithOnConflict(String table, String nullColumnHack,
            ContentValues initialValues, int conflictAlgorithm) throws SQLException {

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
            sql.append(table);
            sql.append('(');
            LOGGER.debug("SQL QUERY: " + sql.toString());
            Object[] bindArgs = null;
            int size = (initialValues != null && initialValues.size() > 0) ? initialValues.size()
                    : 0;
            if (size > 0) {
                bindArgs = new Object[size];
                int i = 0;
                for (String colName : initialValues.keySet()) {
                    sql.append((i > 0) ? "," : "");
                    sql.append(colName);
                    bindArgs[i++] = initialValues.get(colName);
                }
                sql.append(')');
                sql.append(" VALUES (");
                for (i = 0; i < size; i++) {
                    sql.append((i > 0) ? ",?" : "?");
                }
            } else {
                sql.append(nullColumnHack + ") VALUES (NULL");
            }
            sql.append(')');
            LOGGER.debug("SQL QUERY: " + sql.toString());
            PreparedStatement statement = conn.prepareStatement(sql.toString());
            if (bindArgs != null) {
                for (int i = 0; i < bindArgs.length; i++) {
                    statement.setObject(i + 1, bindArgs[i]);
                }
            }
            ResultSet resultSet = null;
            try {
                statement.executeUpdate();
                resultSet = statement.getGeneratedKeys();

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }

            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }
        } finally {

        }
        return -1;
    }

    public int update(String table, ContentValues values, String whereClause) {

        try {
            return updateWithOnConflict(table, values, whereClause, CONFLICT_NONE);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Convenience method for updating rows in the database.
     * 
     * @param table
     *            the table to update in
     * @param values
     *            a map from column names to new column values. null is a valid value that will be
     *            translated to NULL.
     * @param whereClause
     *            the optional WHERE clause to apply when updating. Passing null will update all
     *            rows.
     * @param whereArgs
     *            You may include ?s in the where clause, which will be replaced by the values from
     *            whereArgs. The values will be bound as Strings.
     * @param conflictAlgorithm
     *            for update conflict resolver
     * @return the number of rows affected
     * @throws SQLException
     */
    public int updateWithOnConflict(String table, ContentValues values, String whereClause,
            int conflictAlgorithm) throws SQLException {

        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }

        try {
            StringBuilder sql = new StringBuilder(120);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(table);
            sql.append(" SET ");

            // move all bind args to one array
            int setValuesSize = values.size();
            Object[] bindArgs = new Object[setValuesSize];
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(colName);
                bindArgs[i++] = values.get(colName);
                sql.append("=?");
            }

            if (!StringUtils.isEmpty(whereClause)) {
                sql.append(" WHERE ");
                sql.append(whereClause);
            }

            LOGGER.debug("slq update query: " + sql.toString());

            PreparedStatement statement = conn.prepareStatement(sql.toString());
            if (bindArgs != null) {
                for (int k = 0; k < bindArgs.length; k++) {
                    statement.setObject(k + 1, bindArgs[k]);
                }
            }
            try {
                return statement.executeUpdate();
            } finally {
                statement.close();
            }
        } finally {}
    }

    public ResultSet rawQuery(String sql) throws SQLException {

        LOGGER.debug("SQL Query : " + sql);
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public ResultSet rawQuery(String sql, List<Object> sqlValues) throws SQLException {

        LOGGER.debug("SQL Query : " + sql);
        PreparedStatement statement = conn.prepareStatement(sql);
        if (sqlValues != null && !sqlValues.isEmpty()) {
            LOGGER.debug("adding sqlvalues : " + sql + " " + sqlValues.size() + " stmt values ");
            for (int i = 0; i < sqlValues.size(); i++) {

                LOGGER.debug("adding sqlvalues : " + (i + 1) + " " + sqlValues.get(i));
                try {
                    statement.setObject(i + 1, sqlValues.get(i));
                } catch (Exception ex) {
                    LOGGER.debug(ex.getMessage(), ex);

                }
            }
        }

        return statement.executeQuery();
    }

    public ResultSet rawQueryUnprepared(String sql) throws SQLException {

        LOGGER.debug("SQL Query : " + sql);
        PreparedStatement statement = conn.prepareStatement(sql);
        return statement.executeQuery();
    }

    public int rawUpdateQuery(String sql) throws SQLException {

        LOGGER.debug("SQL Query : " + sql);
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeUpdate();
    }

    private List<String> getCreateTablesQuery() {

        List<String> tableQuery = new ArrayList<String>();
        OrgDataManager.createTables(tableQuery);
        ResourceDataManager.createTables(tableQuery);
        FolderDataManager.createTables(tableQuery);
        SDCardGroupDataManager.createTables(tableQuery);
        SDCardDataManger.createTables(tableQuery);
        SyncInfoDataManager.createTables(tableQuery);
        ImportedLibraryDataManager.createTables(tableQuery);
        FileDownloadInfoDataManager.createTables(tableQuery);
        FlashRecordDataManager.createTables(tableQuery);
        JobInfoDataManager.createTables(tableQuery);
        LOGGER.debug("create Table queries: " + tableQuery);

        return tableQuery;
    }
}
