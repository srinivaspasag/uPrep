package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vedantu.ext.cmds.db.models.FileDownloadInfo;
import com.vedantu.ext.cmds.enums.SQLLITEDataType;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class FileDownloadInfoDataManager extends AbstractDataManager<FileDownloadInfo> {

    private static final String                     TABLE    = "file_download_records";
    public static final FileDownloadInfoDataManager INSTANCE = new FileDownloadInfoDataManager();

    private FileDownloadInfoDataManager() {

        super(FileDownloadInfo.class);
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(FileDownloadInfo model) {

        StringBuilder sb = new StringBuilder();
        sb.append(ConstantGlobal.ENTITY_ID);
        sb.append("='");
        sb.append(model.entityId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.ENTITY_TYPE);
        sb.append("='");
        sb.append(model.entityType);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.TARGET_ID);
        sb.append("='");
        sb.append(model.entityType);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.TARGET_TYPE);
        sb.append("='");
        sb.append(model.entityType);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.NAME);
        sb.append("='");
        sb.append(model.name);
        sb.append("'");
        return sb.toString();
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.NAME + SPACE + SQLLITEDataType.TEXT + SPACE + SQLDBUtils.NOT_NULL
                + ",");
        sb.append(ConstantGlobal.ENTITY_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE + " text not null,");
        sb.append(ConstantGlobal.SIZE + " UNSIGNED BIG INT  not null,");
        sb.append(FileDownloadInfo.FIELD_MEDIA_TYPE + " text not null,");
        sb.append(FileDownloadInfo.FIELD_DOWNLOAD_URL + " text not null,");
        sb.append(FileDownloadInfo.FIELD_TARGET_ID + " text not null,");
        sb.append(FileDownloadInfo.FIELD_TARGET_TYPE + " text not null,");
        sb.append(FileDownloadInfo.FIELD_DOWNLOADED + " BOOLEAN not null ,");
        sb.append(FileDownloadInfo.FIELD_DOWNLOADED_SIZE + " UNSIGNED BIG INT not null,");
        sb.append(FileDownloadInfo.FIELD_DOWNLOAD_START_TIME + " UNSIGNED BIG INT not null,");
        sb.append(FileDownloadInfo.FIELD_DOWNLOAD_END_TIME + " UNSIGNED BIG INT not null ,");
        sb.append(FileDownloadInfo.FIELD_LOCATION + " TEXT NULL ");// can be null before downloading

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "file_download_info_index", true,
                ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE, ConstantGlobal.NAME,
                ConstantGlobal.TARGET_ID, ConstantGlobal.TARGET_TYPE));
    }

    public FileDownloadInfo getFileDownloadInfo(String entityType, String id, String fileName,
            String targetId, String targetType) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_ENTITY_ID, id, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_ENTITY_TYPE, entityType, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_TARGET_ID, targetId, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_TARGET_TYPE, targetType, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        addStringEqualSQLQuery(FileDownloadInfo.FIELD_NAME, fileName, sb, false);

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                FileDownloadInfo fileDownloadInfo = SQLDBUtils.convertToValues(rs,
                        FileDownloadInfo.class, null);
                return fileDownloadInfo;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return null;

    }

    public List<FileDownloadInfo> getFileDownloadInfo(String entityType, String id) {

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(entityType) || StringUtils.isEmpty(id)) {
            return null;
        }

        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_ENTITY_ID, id, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        addStringEqualSQLQuery(FileDownloadInfo.FIELD_ENTITY_TYPE, entityType, sb, false);
        List<FileDownloadInfo> results = new ArrayList<FileDownloadInfo>();

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                FileDownloadInfo fileDownloadInfo = SQLDBUtils.convertToValues(rs,
                        FileDownloadInfo.class, null);
                results.add(fileDownloadInfo);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return results;

    }

    public List<FileDownloadInfo> getFileDownloadInfoFromTarget(String targetId, String targetType) {

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(targetId) || StringUtils.isEmpty(targetType)) {
            return null;
        }

        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(FileDownloadInfo.FIELD_TARGET_ID, targetId, sb, false)) {
            sb.append(KEYWORD_AND);
        }
        addStringEqualSQLQuery(FileDownloadInfo.FIELD_TARGET_TYPE, targetType, sb, false);
        List<FileDownloadInfo> results = new ArrayList<FileDownloadInfo>();

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                FileDownloadInfo fileDownloadInfo = SQLDBUtils.convertToValues(rs,
                        FileDownloadInfo.class, null);
                results.add(fileDownloadInfo);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return results;

    }

    public int getFileDownloadInfoCount(String targetId, String targetType) {

        StringBuilder sb = new StringBuilder();

        sb.append("select  count(*) from " + TABLE + KEYWORD_WHERE + "targetId = ? " + KEYWORD_AND
                + " targetType= ? " + " " + KEYWORD_GROUPBY + " targetId");
        List<Object> values = new ArrayList<Object>();
        values.add(targetId);
        values.add(targetType);

        ResultSet rs = rawQuery(sb.toString(), values);

        try {
            LOGGER.debug("ResultSet size is" + rs.getFetchSize());

            while (rs.next()) {
                LOGGER.debug("Result set values" + rs.getInt(1));
                return rs.getInt(1);

            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }

        return 0;
    }
}
