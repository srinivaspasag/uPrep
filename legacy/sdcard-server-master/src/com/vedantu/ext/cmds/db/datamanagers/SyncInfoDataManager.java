package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vedantu.ext.cmds.db.models.SyncInfo;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class SyncInfoDataManager extends AbstractDataManager<SyncInfo> {

    private static final String             TABLE    = "sync_info";
    public static final SyncInfoDataManager INSTANCE = new SyncInfoDataManager();

    private SyncInfoDataManager() {

        super(SyncInfo.class);
    }

    public SyncInfo getSyncInfo(String key) {
           
        
        SyncInfo syncInfo = null;
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addStringEqualSQLQuery(SyncInfo.FIELD_KEY, key, sb, false);
        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                syncInfo = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        closeResultSet(rs);
        return syncInfo;
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(SyncInfo model) {

        return "key='" + model.key + "'";
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(SyncInfo.FIELD_KEY + " text not null,");
        sb.append(SyncInfo.SYNC_TIME + " text not null");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "sync_info_index", true, SyncInfo.FIELD_KEY));
    }

}
