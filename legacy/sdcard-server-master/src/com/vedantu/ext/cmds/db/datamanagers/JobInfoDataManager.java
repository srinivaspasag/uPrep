package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vedantu.ext.cmds.db.models.Job;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.FieldInfo;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class JobInfoDataManager extends AbstractDataManager<Job> {

    private static final String      TABLE    = "jobs";

    public static JobInfoDataManager INSTANCE = new JobInfoDataManager();

    private JobInfoDataManager() {

        super(Job.class);
    }

    public Job getJobInfo(int jobId) {

        Job job = null;
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addIntEqualSQLQuery(ConstantGlobal._ID, jobId, sb);

        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                job = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }
        return job;
    }

    public void increment(int id, long incrementBy) {

        StringBuilder sb = new StringBuilder();

        FieldInfo fieldInfo = new FieldInfo(Job.FIELD_COMPLETED, Job.FIELD_COMPLETED
                + (incrementBy > 0 ? "+" : "-") + Math.abs(incrementBy));
        LOGGER.debug("Creating update fields for jobId"+ id);
        createUpdateSetQuery(sb, TABLE, fieldInfo);

        sb.append(KEYWORD_WHERE);
        sb.append(ConstantGlobal._ID);
        sb.append("=");
        sb.append(id);
        LOGGER.debug("Update query is "+ sb.toString());
        
        int count = rawUpdate(sb.toString());
        
        LOGGER.debug("Updated number of records" + count);

    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(Job.FIELD_STEPS + " UNSIGNED BIG INT not null,");
        sb.append(Job.FIELD_COMPLETED + " UNSIGNED BIG INT not null,");
        sb.append(Job.FIELD_STATUS + " TEXT not null,");
        sb.append(Job.FIELD_TARGET_ID + " TEXT not null,");
        sb.append(Job.FIELD_TARGET_TYPE + " TEXT not null");
        
        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());

    }

    @Override
    protected String getUpsertQueryCondition(Job model) {

       
        return null;
    }

}
