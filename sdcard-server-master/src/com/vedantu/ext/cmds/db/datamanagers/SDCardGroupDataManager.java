package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vedantu.ext.cmds.db.models.SDCardGroup;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class SDCardGroupDataManager extends AbstractDataManager<SDCardGroup> {

    private static final String                TABLE    = "sd_card_groups";
    public static final SDCardGroupDataManager INSTANCE = new SDCardGroupDataManager();

    private SDCardGroupDataManager() {

        super(SDCardGroup.class);
    }

    public List<SDCardGroup> getSDCardGroups(int orgKeyId, String targetId, String targetType,
            String orderBy, String sortOrder) {

        List<SDCardGroup> cardGroups = new ArrayList<SDCardGroup>();
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(SDCardGroup.FIELD_TARGET_ID, targetId, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(SDCardGroup.FIELD_TARGET_TYPE, targetType, sb, false)) {
            sb.append(KEYWORD_AND);
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        addLimitFilter(sb, orderBy, sortOrder, SQLDBUtils.NO_LIMIT, SQLDBUtils.NO_LIMIT);

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                SDCardGroup sdCardGroup = SQLDBUtils.convertToValues(rs, SDCardGroup.class, null);
                cardGroups.add(sdCardGroup);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        LOGGER.debug("returning sdcard groups: " + cardGroups);
        return cardGroups;
    }

    public int getSDCardGroupCount(int orgKeyId, String targetId, String targetType) {

        StringBuilder sb = new StringBuilder();
        String[] fields = { "count(*) as group_count " };
        addSelectQuery(sb, fields);

        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(SDCardGroup.FIELD_TARGET_ID, targetId, sb, false)) {
            sb.append(KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(SDCardGroup.FIELD_TARGET_TYPE, targetType, sb, false)) {
            sb.append(KEYWORD_AND);
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        ResultSet rs = rawQuery(sb.toString());
        try {
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

    public SDCardGroup getSDCardGroup(String id) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false);

        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                SDCardGroup sdCardGroup = SQLDBUtils.convertToValues(rs, SDCardGroup.class, null);
                return sdCardGroup;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return null;
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(SDCardGroup model) {

        return "id='" + model.id + "'";
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_TYPE + " text not null,");
        sb.append(SDCardGroup.FIELD_SIZE + " text not null,");
        sb.append(SDCardGroup.FIELD_CARD_SIZE + " text not null,");
        sb.append(SDCardGroup.FIELD_NO_OF_CARDS + " integer,");
        sb.append(SDCardGroup.FIELD_CARD_IDS + " text not null");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "sdcard_group_index", true,
                ConstantGlobal.TARGET_ID, ConstantGlobal.TARGET_TYPE, ConstantGlobal.ID));
    }

}
